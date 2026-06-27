# ---------------------------------------------------------------------------
# Root module: assembles the environment-variable map the container needs
# (mirrors application-prod.yaml) and hands it to the reusable render_app
# module that creates the Render web service.
# ---------------------------------------------------------------------------

# When a project id is supplied, look it up so we can resolve the environment
# id from the human-friendly environment name (e.g. "dev").
data "render_project" "this" {
  count = var.render_project_id != null ? 1 : 0
  id    = var.render_project_id
}

locals {
  # Resolve which environment id (evm-xxxx) the service should be attached to:
  #   1. an explicit render_environment_id wins, else
  #   2. look up render_environment_name inside the given project, else
  #   3. null -> created outside any project.
  resolved_environment_id = (
    var.render_environment_id != null
    ? var.render_environment_id
    : (
      var.render_project_id != null
      ? data.render_project.this[0].environments[var.render_environment_name].id
      : null
    )
  )

  # Every variable application-prod.yaml resolves from the environment.
  # Keep this in sync with src/main/resources/application-prod.yaml.
  app_env_vars = {

    # --- App / server / logging ---
    SPRING_PROFILES_ACTIVE = "prod"
    LOGGING_LEVEL          = var.logging_level
    DB_MODE                = var.db_mode

    # --- Database ---
    DB_URL      = var.db_url
    DB_USERNAME = var.db_username
    DB_PASSWORD = var.db_password
    DB_NAME     = var.db_name


    # --- RabbitMQ connection ---
    RABBITMQ_HOST     = var.rabbitmq_host
    RABBITMQ_PORT     = var.rabbitmq_port
    RABBITMQ_USERNAME = var.rabbitmq_username
    RABBITMQ_PASSWORD = var.rabbitmq_password
    RABBITMQ_VHOST    = var.rabbitmq_vhost

    # --- RabbitMQ: matches ---
    RABBITMQ_MATCHES_EXCHANGE    = var.rabbitmq_matches.exchange
    RABBITMQ_MATCHES_QUEUE       = var.rabbitmq_matches.queue
    RABBITMQ_MATCHES_ROUTING_KEY = var.rabbitmq_matches.routing_key

    # --- RabbitMQ: results ---
    RABBITMQ_RESULTS_EXCHANGE    = var.rabbitmq_results.exchange
    RABBITMQ_RESULTS_QUEUE       = var.rabbitmq_results.queue
    RABBITMQ_RESULTS_ROUTING_KEY = var.rabbitmq_results.routing_key

    # --- RabbitMQ: transactions ---
    RABBITMQ_TRANSACTIONS_EXCHANGE    = var.rabbitmq_transactions.exchange
    RABBITMQ_TRANSACTIONS_QUEUE       = var.rabbitmq_transactions.queue
    RABBITMQ_TRANSACTIONS_ROUTING_KEY = var.rabbitmq_transactions.routing_key

    # --- RabbitMQ: wallets ---
    RABBITMQ_WALLETS_EXCHANGE    = var.rabbitmq_wallets.exchange
    RABBITMQ_WALLETS_QUEUE       = var.rabbitmq_wallets.queue
    RABBITMQ_WALLETS_ROUTING_KEY = var.rabbitmq_wallets.routing_key
  }
}

module "event_consumer" {
  source = "./modules/render_app"

  name           = var.service_name
  region         = var.region
  plan           = var.plan
  docker_image   = var.docker_image
  image_tag      = var.image_tag
  environment_id = local.resolved_environment_id


  env_vars = local.app_env_vars

}
