# Inputs for the reusable render_app module.

variable "name" {
  description = "Name of the Render web service."
  type        = string
}

variable "region" {
  description = "Render region to deploy into."
  type        = string
  default     = "oregon"
}

variable "plan" {
  description = "Render instance plan."
  type        = string
  default     = "starter"
}

variable "docker_image" {
  description = "Docker Hub image repository (without tag), e.g. bixoloo/event-consumer."
  type        = string
}

variable "image_tag" {
  description = "Image tag to deploy."
  type        = string
  default     = "latest"
}

variable "env_vars" {
  description = "Map of environment variables to inject into the container."
  type        = map(string)
  default     = {}
}

variable "environment_id" {
  description = "Optional Render Environment id (evm-xxxx) to place the service under a Project/Environment. Null = not attached to any project."
  type        = string
  default     = null
}

