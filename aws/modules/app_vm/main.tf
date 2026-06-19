# Generic single-container application VM (Docker + one container).

resource "aws_instance" "this" {
  ami                    = var.ami_id
  instance_type          = var.instance_type
  subnet_id              = var.subnet_id
  vpc_security_group_ids = var.security_group_ids
  key_name               = var.key_name

  associate_public_ip_address = var.associate_public_ip

  root_block_device {
    volume_size = var.root_volume_gb
    volume_type = "gp3"
    encrypted   = true
  }

  user_data = templatefile("${path.module}/templates/app-user-data.sh.tftpl", {
    container_name     = var.container_name
    image              = var.image
    port               = var.port
    dockerhub_username = var.dockerhub_username
    dockerhub_token    = var.dockerhub_token
    env_file           = var.env_file
  })

  user_data_replace_on_change = true

  tags = merge({ Name = var.name }, var.tags)
}
