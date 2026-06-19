# --- Provider auth ---------------------------------------------------------
variable "render_api_key" {
  description = "Render API key. Prefer setting via TF_VAR_render_api_key / RENDER_API_KEY."
  type        = string
  sensitive   = true
}

variable "render_owner_id" {
  description = "Render owner (team or user) id, e.g. tea-xxxx or usr-xxxx."
  type        = string
}

variable "render_environment_id" {
  description = <<-EOT
    Optional Render Environment id (evm-xxxx) of the Project/Environment to
    place this service under. Render organizes services into Projects, each
    with one or more Environments (e.g. production, staging); a service is
    attached to an environment via its id. Leave null to create the service
    outside of any project.

    Usually you do NOT set this directly — instead set render_project_id +
    render_environment_name and let the project data source resolve the id.
    If set, this takes precedence over the project/name lookup.
  EOT
  type        = string
  default     = null
}

variable "render_project_id" {
  description = <<-EOT
    Optional Render Project id (prj-xxxx) to place this service under. When
    combined with render_environment_name, the matching environment id is
    looked up automatically via the render_project data source. Leave null to
    create the service outside of any project.
  EOT
  type        = string
  default     = null
}

variable "render_environment_name" {
  description = "Environment name within render_project_id to deploy into (e.g. \"dev\", \"production\"). Used only when render_project_id is set and render_environment_id is not."
  type        = string
  default     = "production"
}



# --- Service basics --------------------------------------------------------
variable "service_name" {
  description = "Name of the Render web service."
  type        = string
  default     = "event-consumer"
}

variable "region" {
  description = "Render region to deploy into (e.g. oregon, frankfurt, singapore, ohio, virginia)."
  type        = string
  default     = "frankfurt"

}

variable "plan" {
  description = "Render instance plan (e.g. starter, standard, pro)."
  type        = string
  default     = "starter"
}

variable "docker_image" {
  description = "Fully-qualified image repo on Docker Hub (without tag)."
  type        = string
  default     = "bixoloo/event-consumer"
}

variable "image_tag" {
  description = "Image tag to deploy (e.g. latest or a specific git SHA)."
  type        = string
  default     = "latest"
}

# --- App / logging ---------------------------------------------------------
variable "logging_level" {
  description = "Application logging level (INFO, DEBUG, WARN, ERROR)."
  type        = string
  default     = "INFO"
}

variable "db_mode" {
  description = "Spring sql.init mode in prod. Keep 'never' so the managed schema isn't reset on every boot."
  type        = string
  default     = "never"
}

# --- Database (managed externally; credentials injected as env vars) -------
variable "db_url" {
  description = "JDBC URL for the MySQL database."
  type        = string
}

variable "db_username" {
  description = "Database username."
  type        = string
}

variable "db_password" {
  description = "Database password."
  type        = string
  sensitive   = true
}

variable "db_name" {
  description = "Database name."
  type        = string
}

variable "db_host" {
  description = "Database host"
  type        = string
}

variable "db_port" {
  description = "Database port"
  type        = number
}

variable "ssl_mode" {
  description = "Database ssl mode"
  type        = string
}
# --- RabbitMQ connection ---------------------------------------------------
variable "rabbitmq_host" {
  description = "RabbitMQ host."
  type        = string
}

variable "rabbitmq_port" {
  description = "RabbitMQ port."
  type        = string
  default     = "5672"
}

variable "rabbitmq_username" {
  description = "RabbitMQ username."
  type        = string
}

variable "rabbitmq_password" {
  description = "RabbitMQ password."
  type        = string
  sensitive   = true
}

variable "rabbitmq_vhost" {
  description = "RabbitMQ virtual host."
  type        = string
  default     = "/"
}

variable "rabbitmq_url" {
  description = "RabbitMQ host url"
  type        = string
  sensitive   = true
}

# --- RabbitMQ topology (exchange / queue / routing-key per domain) ---------
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
