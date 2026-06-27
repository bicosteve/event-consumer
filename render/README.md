# Infrastructure (Terraform → Render)

Infrastructure-as-Code that deploys the **event-consumer** service to
[Render](https://render.com) as a **Background Worker** running a Docker image.

`event-consumer` is a consumer/worker: it reads from RabbitMQ and writes to
MySQL, and does **not** serve inbound HTTP traffic. The correct Render primitive
for that is a Background Worker (not a Web Service) — so it has **no public URL,
no port binding requirement, and no inbound network exposure**, which is both
more accurate to the workload and more secure.

The image is the one built and pushed to Docker Hub (`bixoloo/event-consumer`)
by the CI pipeline, so Terraform only manages the *runtime* (service, region,
plan, and environment variables) — it does not build the image.


## Layout

```
infra/
├── versions.tf              # required_version + render provider constraint
├── providers.tf             # render provider auth (api key + owner id)
├── variables.tf             # all root inputs (service, db, rabbitmq, ...)
├── main.tf                  # builds the env-var map, calls the module
├── outputs.tf               # service id
├── terraform.tfvars.example # copy -> terraform.tfvars, fill in
└── modules/
    └── render_app/          # reusable "Render service from image" module
        ├── versions.tf
        ├── variables.tf
        ├── main.tf          # render_background_worker resource
        └── outputs.tf
```


The root module is intentionally thin: it assembles the environment variables
that mirror `src/main/resources/application-prod.yaml` and delegates the actual
resource creation to the `render_app` module. The module can be reused for any
other image-based Render service.

## Prerequisites

- Terraform >= 1.5
- A Render account + **API key** (Account Settings → API Keys)
- Your Render **owner id** (team `tea-...` or user `usr-...`)
- *(Optional)* A Render **Project/Environment id** (`evm-...`) if you want the
  service grouped under a Project — set `render_environment_id`. Render organizes
  services into Projects, each containing one or more Environments (e.g.
  production, staging). Leave it unset to create the service outside any project.
- The image already published to Docker Hub by CI

- A reachable MySQL database and RabbitMQ broker (managed outside this stack)

## Usage

```bash
cd infra

# 1. Provide secrets via environment (preferred — keeps them out of files)
export TF_VAR_render_api_key="rnd_xxxxxxxx"
export TF_VAR_db_password="..."
export TF_VAR_rabbitmq_password="..."

# 2. Provide non-secret values
cp terraform.tfvars.example terraform.tfvars
#   ...edit terraform.tfvars (owner id, db_url, hosts, etc.)

# 3. Standard workflow
terraform init
terraform fmt -recursive
terraform validate
terraform plan
terraform apply
```

Outputs after apply:

```bash
terraform output service_id    # Render service id (no URL — it's a worker)
```


## Deploying a new image version

CI pushes both `:latest` and `:<git-sha>` tags. To pin a specific build:

```bash
terraform apply -var="image_tag=<git-sha>"
```

Leaving `image_tag = "latest"` deploys whatever currently points at `latest`.

## Secrets & state

- **Never commit** `terraform.tfvars` or `*.tfstate` — both are gitignored.
- Sensitive variables (`render_api_key`, `db_password`, `rabbitmq_password`)
  are marked `sensitive` and are best supplied via `TF_VAR_*` env vars.
- For team use, configure a remote backend (e.g. Terraform Cloud or an S3 +
  DynamoDB backend) so state is shared and locked rather than living on one
  machine. Add a `backend` block to `versions.tf` when you do.
```
