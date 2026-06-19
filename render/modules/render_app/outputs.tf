# Outputs exposed by the render_app module.
#
# Note: a Background Worker has no public URL, so only the service id is
# surfaced (useful for referencing the service in the Render dashboard/API).
output "service_id" {
  description = "Render service id."
  value       = render_background_worker.this.id
}
