# Module provider constraints. The provider is configured in the root module
# and inherited here.
terraform {
  required_version = ">= 1.5.0"

  required_providers {
    render = {
      source  = "render-oss/render"
      version = "~> 1.4"
    }
  }
}
