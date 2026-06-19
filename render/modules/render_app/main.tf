# ---------------------------------------------------------------------------
# render_app module
#
# event-consumer is a CONSUMER/WORKER: it reads from RabbitMQ and writes to
# MySQL — it does not serve inbound HTTP traffic. The correct Render primitive
# for a long-running process with no public endpoint is a Background Worker
# (not a Web Service). This means no public URL, no port binding requirement,
# and no inbound network exposure — which is both more correct and more secure
# for this workload.
#
# It runs the prebuilt image from Docker Hub (the artifact the CI pipeline
# builds and pushes).
# ---------------------------------------------------------------------------

resource "render_background_worker" "this" {
  name   = var.name
  plan   = var.plan
  region = var.region

  # Attach to a Render Project/Environment when an id is supplied; otherwise
  # the service is created outside of any project.
  environment_id = var.environment_id


  # Deploy the prebuilt image from Docker Hub rather than building from source.
  runtime_source = {
    image = {
      image_url = "docker.io/${var.docker_image}"
      tag       = var.image_tag
    }
  }

  # Inject all application environment variables.
  env_vars = {
    for k, v in var.env_vars : k => { value = v }
  }
}
