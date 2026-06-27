# ---------------------------------------------------------------------------
# Inputs. Secrets default to TF_VAR_* env vars; non-secrets have sane defaults
# you can override in terraform.tfvars.
#
# Topology (4 VMs, all t2.micro, in your existing VPC):
#   PUBLIC subnet  (web_access_SG):  api-gateway, rapid-engine
#   PRIVATE subnet (data_SG):        event-consumer, data-vm (MySQL+RabbitMQ+Redis)
# ---------------------------------------------------------------------------

# --- Provider / general ----------------------------------------------------
variable "region" {
  description = "AWS region to deploy into."
  type        = string
  default     = "eu-central-1"
}

variable "aws_profile" {
  description = "Optional named AWS CLI profile. Leave null to use the default credential chain / env vars."
  type        = string
  default     = null
}

variable "environment" {
  description = "Environment name used for tagging/naming (e.g. dev, prod)."
  type        = string
  default     = "dev"
}

variable "name_prefix" {
  description = "Prefix applied to resource names."
  type        = string
  default     = "sportsbook"
}

# --- Existing network (looked up by id) ------------------------------------
variable "vpc_id" {
  description = "Id of your existing (non-default) VPC, e.g. vpc-0123456789abcdef0."
  type        = string
}

variable "public_subnet_id" {
  description = "PUBLIC subnet id (route to an internet gateway). Hosts api-gateway + rapid-engine."
  type        = string
}

variable "private_subnet_id" {
  description = "PRIVATE subnet id. Hosts event-consumer + the data VM. Needs a NAT gateway for outbound internet (image/package pulls)."
  type        = string
}

# --- Existing security groups (looked up by id) ----------------------------
variable "web_access_sg_id" {
  description = "Existing security group id for PUBLIC instances (web_access_SG). Should allow your app/HTTP ports + SSH inbound."
  type        = string
}

variable "data_sg_id" {
  description = "Existing security group id for PRIVATE instances (data_SG). Should allow internal traffic (MySQL/RabbitMQ/Redis/SSH) within the VPC."
  type        = string
}

# --- Access ----------------------------------------------------------------
variable "ssh_public_key_path" {
  description = "Path to a local SSH public key to install on the instances (e.g. ~/.ssh/id_ed25519.pub). Only used when existing_key_name is null."
  type        = string
  default     = null
}

variable "existing_key_name" {
  description = "Name of an EC2 key pair that ALREADY exists in this AWS region (e.g. the one you use on your other VMs). Set this to reuse it on the new VMs instead of importing a new public key. When set, ssh_public_key_path is ignored."
  type        = string
  default     = null
}

# --- Compute ---------------------------------------------------------------
variable "instance_type" {
  description = "EC2 instance type for all VMs."
  type        = string
  default     = "t2.micro"
}

variable "root_volume_gb" {
  description = "Root EBS volume size (GiB) for app VMs."
  type        = number
  default     = 8
}

variable "data_volume_gb" {
  description = "Size (GiB) of the dedicated EBS data volume on the data VM (MySQL/RabbitMQ/Redis persistence)."
  type        = number
  default     = 20
}

# --- Container images ------------------------------------------------------
variable "api_gateway_image" {
  description = "Docker Hub image (with or without tag) for the api-gateway REST API."
  type        = string
  default     = "bixoloo/api-gateway:latest"
}

variable "rapid_engine_image" {
  description = "Docker Hub image for the rapid-engine worker."
  type        = string
  default     = "bixoloo/rapid-engine:latest"
}

variable "event_consumer_image" {
  description = "Docker Hub image for the event-consumer worker."
  type        = string
  default     = "bixoloo/event-consumer:latest"
}

variable "dockerhub_username" {
  description = "Docker Hub username (only needed if any image is private)."
  type        = string
  default     = null
}

variable "dockerhub_token" {
  description = "Docker Hub access token/password (only needed if any image is private)."
  type        = string
  sensitive   = true
  default     = null
}

# --- App config / logging --------------------------------------------------
variable "logging_level" {
  description = "Application logging level."
  type        = string
  default     = "INFO"
}

variable "db_mode" {
  description = "Spring sql.init mode. 'always' on first boot to load schema.sql, then 'never'."
  type        = string
  default     = "never"
}

variable "api_gateway_port" {
  description = "Port the api-gateway container listens on (also published on the host)."
  type        = number
  default     = 5001
}

variable "rapid_engine_port" {
  description = "Port the api-gateway container listens on (also published on the host)."
  type        = number
  default     = 5002
}

variable "event_consume_port" {
  description = "Port the api-gateway container listens on (also published on the host)."
  type        = number
  default     = 5003
}

# --- Data services (installed natively on the data VM) ---------------------
variable "mysql_root_password" {
  description = "MySQL root password."
  type        = string
  sensitive   = true
}

variable "db_name" {
  description = "Application database name to create."
  type        = string
  default     = "sportsbook"
}

variable "db_username" {
  description = "Application database user to create."
  type        = string
  default     = "appuser"
}

variable "db_password" {
  description = "Application database user password."
  type        = string
  sensitive   = true
}

variable "rabbitmq_username" {
  description = "RabbitMQ user to create on the data VM."
  type        = string
  default     = "appuser"
}

variable "rabbitmq_password" {
  description = "RabbitMQ password."
  type        = string
  sensitive   = true
}

variable "rabbitmq_vhost" {
  description = "RabbitMQ virtual host to create."
  type        = string
  default     = "/"
}

variable "redis_password" {
  description = "Password (requirepass) for Redis on the data VM. Empty string disables auth (not recommended)."
  type        = string
  sensitive   = true
  default     = ""
}

# --- External API (rapid-engine source) ------------------------------------
variable "rundown_api_key" {
  description = "API key for the Rundown API that rapid-engine reads from."
  type        = string
  sensitive   = true
  default     = ""
}

variable "rundown_api_host" {
  description = "Base host for the Rundown API."
  type        = string
  default     = "https://therundown-therundown-v1.p.rapidapi.com"
}

# --- RabbitMQ topology (exchanges/queues/routing keys) ---------------------
variable "rabbitmq_matches" {
  description = "Matches exchange/queue/routing-key."
  type = object({
    exchange    = string
    queue       = string
    routing_key = string
  })
  default = {
    exchange    = "matches.exchange"
    queue       = "matches.queue"
    routing_key = "matches.routing.key"
  }
}

variable "rabbitmq_results" {
  description = "Results exchange/queue/routing-key."
  type = object({
    exchange    = string
    queue       = string
    routing_key = string
  })
  default = {
    exchange    = "results.exchange"
    queue       = "results.queue"
    routing_key = "results.routing.key"
  }
}

variable "rabbitmq_transactions" {
  description = "Transactions exchange/queue/routing-key."
  type = object({
    exchange    = string
    queue       = string
    routing_key = string
  })
  default = {
    exchange    = "transactions.exchange"
    queue       = "transactions.queue"
    routing_key = "transactions.routing.key"
  }
}

variable "rabbitmq_wallets" {
  description = "Wallets exchange/queue/routing-key."
  type = object({
    exchange    = string
    queue       = string
    routing_key = string
  })
  default = {
    exchange    = "wallets.exchange"
    queue       = "wallets.queue"
    routing_key = "wallets.routing.key"
  }
}
