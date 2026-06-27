# Render provider configuration.
#
# Authentication & owner are read from variables (which themselves default to
# environment variables) so no secrets are ever committed:
#   - render_api_key  -> RENDER_API_KEY   (Account Settings -> API Keys)
#   - render_owner_id -> RENDER_OWNER_ID  (the team/user id, e.g. tea-xxxx / usr-xxxx)
provider "render" {
  api_key  = var.render_api_key
  owner_id = var.render_owner_id
}
