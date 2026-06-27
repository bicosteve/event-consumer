# ---------------------------------------------------------------------------
# Root composition: wires the reusable modules together.
#
#   module.data            -> data_vm  (private)  MySQL + RabbitMQ + Redis
#   module.event_consumer  -> app_vm   (private)  worker
#   module.rapid_engine    -> app_vm   (public)   worker
#   module.api_gateway     -> app_vm   (public)   REST API
#
# Network/AMI/key lookups live in the network module (see network.tf);
# env-file bodies are built below from the data VM's private IP.
# ---------------------------------------------------------------------------

locals {
  data_ip = module.data.private_ip

  db_url = "jdbc:mysql://${local.data_ip}:3306/${var.db_name}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"

  env_db = <<-EOT
    SPRING_PROFILES_ACTIVE=prod
    LOGGING_LEVEL=${var.logging_level}
    DB_MODE=${var.db_mode}
    DB_URL=${local.db_url}
    DB_NAME=${var.db_name}
    DB_USERNAME=${var.db_username}
    DB_PASSWORD=${var.db_password}
  EOT

  env_rabbit = <<-EOT
    RABBITMQ_HOST=${local.data_ip}
    RABBITMQ_PORT=5672
    RABBITMQ_USERNAME=${var.rabbitmq_username}
    RABBITMQ_PASSWORD=${var.rabbitmq_password}
    RABBITMQ_VHOST=${var.rabbitmq_vhost}
    RABBITMQ_MATCHES_EXCHANGE=${var.rabbitmq_matches.exchange}
    RABBITMQ_MATCHES_QUEUE=${var.rabbitmq_matches.queue}
    RABBITMQ_MATCHES_ROUTING_KEY=${var.rabbitmq_matches.routing_key}
    RABBITMQ_RESULTS_EXCHANGE=${var.rabbitmq_results.exchange}
    RABBITMQ_RESULTS_QUEUE=${var.rabbitmq_results.queue}
    RABBITMQ_RESULTS_ROUTING_KEY=${var.rabbitmq_results.routing_key}
    RABBITMQ_TRANSACTIONS_EXCHANGE=${var.rabbitmq_transactions.exchange}
    RABBITMQ_TRANSACTIONS_QUEUE=${var.rabbitmq_transactions.queue}
    RABBITMQ_TRANSACTIONS_ROUTING_KEY=${var.rabbitmq_transactions.routing_key}
    RABBITMQ_WALLETS_EXCHANGE=${var.rabbitmq_wallets.exchange}
    RABBITMQ_WALLETS_QUEUE=${var.rabbitmq_wallets.queue}
    RABBITMQ_WALLETS_ROUTING_KEY=${var.rabbitmq_wallets.routing_key}
  EOT

  env_redis = <<-EOT
    REDIS_HOST=${local.data_ip}
    REDIS_PORT=6379
    REDIS_PASSWORD=${var.redis_password}
  EOT

  common_tags = {
    Project     = var.name_prefix
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}

# --- Data VM (private) ------------------------------------------------------
module "data" {
  source = "./modules/data_vm"

  name               = "${var.name_prefix}-data"
  ami_id             = module.network.ami_id
  instance_type      = var.instance_type
  subnet_id          = module.network.private_subnet_id
  availability_zone  = module.network.private_subnet_az
  security_group_ids = [module.network.data_sg_id]
  key_name           = module.network.key_name
  root_volume_gb     = var.root_volume_gb
  data_volume_gb     = var.data_volume_gb

  mysql_root_password = var.mysql_root_password
  db_name             = var.db_name
  db_username         = var.db_username
  db_password         = var.db_password
  rabbitmq_username   = var.rabbitmq_username
  rabbitmq_password   = var.rabbitmq_password
  rabbitmq_vhost      = var.rabbitmq_vhost
  redis_password      = var.redis_password

  tags = local.common_tags
}

# --- event-consumer (private worker) ---------------------------------------
module "event_consumer" {
  source = "./modules/app_vm"

  name                = "${var.name_prefix}-event-consumer"
  container_name      = "event-consumer"
  image               = var.event_consumer_image
  ami_id              = module.network.ami_id
  instance_type       = var.instance_type
  subnet_id           = module.network.private_subnet_id
  security_group_ids  = [module.network.data_sg_id]
  key_name            = module.network.key_name
  associate_public_ip = false
  root_volume_gb      = var.root_volume_gb
  port                = null
  dockerhub_username  = var.dockerhub_username
  dockerhub_token     = var.dockerhub_token

  env_file = join("\n", [local.env_db, local.env_rabbit, local.env_redis])

  tags = local.common_tags
}

# --- rapid-engine (public worker) ------------------------------------------
module "rapid_engine" {
  source = "./modules/app_vm"

  name                = "${var.name_prefix}-rapid-engine"
  container_name      = "rapid-engine"
  image               = var.rapid_engine_image
  ami_id              = module.network.ami_id
  instance_type       = var.instance_type
  subnet_id           = module.network.public_subnet_id
  security_group_ids  = [module.network.web_access_sg_id]
  key_name            = module.network.key_name
  associate_public_ip = true
  root_volume_gb      = var.root_volume_gb
  port                = null
  dockerhub_username  = var.dockerhub_username
  dockerhub_token     = var.dockerhub_token

  env_file = join("\n", [
    local.env_rabbit,
    local.env_redis,
    <<-EOT
      SPRING_PROFILES_ACTIVE=prod
      LOGGING_LEVEL=${var.logging_level}
      RUNDOWN_API_KEY=${var.rundown_api_key}
      RUNDOWN_API_HOST=${var.rundown_api_host}
    EOT
  ])

  tags = local.common_tags
}

# --- api-gateway (public, internet-facing) ---------------------------------
module "api_gateway" {
  source = "./modules/app_vm"

  name                = "${var.name_prefix}-api-gateway"
  container_name      = "api-gateway"
  image               = var.api_gateway_image
  ami_id              = module.network.ami_id
  instance_type       = var.instance_type
  subnet_id           = module.network.public_subnet_id
  security_group_ids  = [module.network.web_access_sg_id]
  key_name            = module.network.key_name
  associate_public_ip = true
  root_volume_gb      = var.root_volume_gb
  port                = var.api_gateway_port
  dockerhub_username  = var.dockerhub_username
  dockerhub_token     = var.dockerhub_token

  env_file = join("\n", [
    local.env_db,
    local.env_rabbit,
    local.env_redis,
    "SERVER_PORT=${var.api_gateway_port}",
  ])

  tags = local.common_tags
}
