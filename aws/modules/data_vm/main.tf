# Data VM (MySQL + RabbitMQ + Redis native) with a dedicated persistent EBS volume.

resource "aws_instance" "this" {
  ami                    = var.ami_id
  instance_type          = var.instance_type
  subnet_id              = var.subnet_id
  vpc_security_group_ids = var.security_group_ids
  key_name               = var.key_name

  associate_public_ip_address = false

  root_block_device {
    volume_size = var.root_volume_gb
    volume_type = "gp3"
    encrypted   = true
  }

  user_data = templatefile("${path.module}/templates/data-user-data.sh.tftpl", {
    mysql_root_password = var.mysql_root_password
    db_name             = var.db_name
    db_username         = var.db_username
    db_password         = var.db_password
    rabbitmq_username   = var.rabbitmq_username
    rabbitmq_password   = var.rabbitmq_password
    rabbitmq_vhost      = var.rabbitmq_vhost
    redis_password      = var.redis_password
  })

  user_data_replace_on_change = true

  tags = merge({ Name = var.name }, var.tags)
}

resource "aws_ebs_volume" "data" {
  availability_zone = var.availability_zone
  size              = var.data_volume_gb
  type              = "gp3"
  encrypted         = true

  tags = merge({ Name = "${var.name}-vol" }, var.tags)
}

resource "aws_volume_attachment" "data" {
  device_name                    = "/dev/sdf"
  volume_id                      = aws_ebs_volume.data.id
  instance_id                    = aws_instance.this.id
  stop_instance_before_detaching = true
}
