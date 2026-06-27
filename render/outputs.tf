# Root module outputs (surfaced from the render_app module).
#
# event-consumer is a Background Worker, so there is no public URL to expose —
# only the service id is surfaced.
output "service_id" {
  description = "Render service id of the deployed background worker."
  value       = module.event_consumer.service_id
}
