# AWS provider. Credentials are NOT set here — supply them via the standard
# AWS mechanisms (env vars AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY, a shared
# profile, or an SSO/instance role) so nothing sensitive lives in the repo.

provider "aws" {
  region  = var.region
  profile = var.aws_profile # optional; null = default credential chain

  default_tags {
    tags = {
      Project   = "event-consumer"
      ManagedBy = "terraform"
      Env       = var.environment
    }
  }
}
