# Network: looks up the existing VPC/subnets/SGs + AMI and creates the SSH key
# pair. No networking is created here — everything is referenced by id.

module "network" {
  source = "./modules/network"

  name_prefix         = var.name_prefix
  vpc_id              = var.vpc_id
  public_subnet_id    = var.public_subnet_id
  private_subnet_id   = var.private_subnet_id
  web_access_sg_id    = var.web_access_sg_id
  data_sg_id          = var.data_sg_id
  ssh_public_key_path = var.ssh_public_key_path
  existing_key_name   = var.existing_key_name
}
