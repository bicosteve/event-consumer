# AWS deployment (4 VMs in an existing VPC)

Terraform that deploys the sportsbook platform as **four `t2.micro` EC2 instances**
inside a VPC, **two subnets, and two security groups you already have**. Nothing
networking-wise is created here except the SSH key pair and the data EBS volume —
the VPC, subnets and SGs are looked up by id.

## Topology

```
                 Internet
                    │  (Internet Gateway)
        ┌───────────┴────────────┐  PUBLIC subnet  (web_access_SG)
        │ api-gateway  (public IP)│  REST API, internet-facing
        │ rapid-engine (public IP)│  worker → Rundown API + publishes to RabbitMQ
        └───────────┬────────────┘
                    │ VPC-internal
        ┌───────────┴────────────┐  PRIVATE subnet (data_SG)
        │ event-consumer          │  worker, no public IP
        │ data-vm                 │  MySQL + RabbitMQ + Redis (native), EBS /data
        └─────────────────────────┘
```

| VM              | Subnet  | SG             | Public IP | Role |
|-----------------|---------|----------------|-----------|------|
| `api-gateway`   | public  | web_access_SG  | yes       | Internet-facing REST API |
| `rapid-engine`  | public  | web_access_SG  | yes       | Reads Rundown API, publishes matches |
| `event-consumer`| private | data_SG        | no        | Consumes queues, settles bets |
| `data-vm`       | private | data_SG        | no        | MySQL, RabbitMQ, Redis (persisted on EBS) |

## Architecture review (important)

The requested layout is sound, with one caveat:

- `api-gateway` public — correct, it must accept internet traffic.
- `data-vm` + `event-consumer` private — correct, never internet-reachable.
- `rapid-engine` public — it's a **worker** with no inbound needs. Putting it
  in the public subnet is a deliberate **cost optimization**: it reaches the
  internet (Rundown API, Docker Hub) via the free Internet Gateway instead of a
  paid **NAT gateway**. The tradeoff is a public IP / larger attack surface.
  Mitigate by keeping `web_access_SG` inbound limited to **SSH (and only the
  api-gateway app port)** — do not open app ports to rapid-engine.

The textbook-secure alternative is to move `rapid-engine` to the private subnet
and add a NAT gateway; flip `associate_public_ip_address` + `subnet_id` in
`rapid-engine.tf` if you go that route.

### NAT requirement for private VMs

`event-consumer` and `data-vm` are in the **private** subnet. To pull Docker
images / OS packages on first boot they need outbound internet via a **NAT
gateway** on that subnet's route table. If you have no NAT, you must either add
one, bake images into a custom AMI, or temporarily place them in a public subnet
for bootstrap.

## Layout (modularized)

```
aws/
├── main.tf            # root composition: calls modules, builds env files
├── network.tf         # calls the network module
├── variables.tf       # root inputs
├── outputs.tf         # root outputs (from module outputs)
├── providers.tf       # AWS provider + default tags
├── versions.tf        # required Terraform/provider versions
└── modules/
    ├── network/       # looks up existing VPC/subnets/SGs + AMI, creates key pair
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    ├── app_vm/        # reusable single-container VM (api-gateway, rapid-engine, event-consumer)
    │   ├── main.tf
    │   ├── variables.tf
    │   ├── outputs.tf
    │   └── templates/app-user-data.sh.tftpl
    └── data_vm/       # data VM: MySQL+RabbitMQ+Redis native + EBS volume
        ├── main.tf
        ├── variables.tf
        ├── outputs.tf
        └── templates/data-user-data.sh.tftpl
```

The root module owns only **composition**; the child modules own everything else:

- `network` — encapsulates the existing-infra **lookups** (VPC/subnets/SGs/AMI)
  and creates the SSH key pair, exposing ids/AMI/key-name as outputs. (It only
  *reads* networking; nothing is created except the key pair.)
- `app_vm` — reusable single-container VM, **instantiated three times**
  (api-gateway, rapid-engine, event-consumer), differing only by inputs
  (subnet/SG/public-ip/port/env).
- `data_vm` — the stateful data node plus its dedicated EBS volume.

This follows the standard root + child-module pattern: small, single-purpose,
reusable modules with explicit inputs/outputs.


## Usage

```bash
cd aws
cp terraform.tfvars.example terraform.tfvars   # fill in ids + secrets

# Prefer env vars for secrets:
export TF_VAR_mysql_root_password=...
export TF_VAR_db_password=...
export TF_VAR_rabbitmq_password=...
export TF_VAR_redis_password=...
export TF_VAR_rundown_api_key=...
# export TF_VAR_dockerhub_token=...   # only if images are private

terraform init
terraform plan
terraform apply
```

Outputs give you the public IPs, the api-gateway URL, and SSH commands
(including a `-J` bastion jump to the private data VM through api-gateway).

## Notes

- All app config is passed via a Docker `--env-file`; apps run with
  `--restart unless-stopped`.
- On first deploy set `db_mode = "always"` so the api-gateway/event-consumer load
  `schema.sql`; switch back to `"never"` afterwards.
- Editing user-data forces instance replacement (`user_data_replace_on_change`).
- The data VM keeps MySQL/RabbitMQ/Redis state on a separate gp3 EBS volume at
  `/data`, so it survives instance replacement of the OS root.
