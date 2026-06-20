<#
.SYNOPSIS
  Abre um túnel SSH temporário para o MySQL de produção.

.DESCRIPTION
  O MySQL roda dentro do container `db` e é publicado APENAS em 127.0.0.1:3306
  no host de produção (ver docker-compose.yml) - não fica exposto na internet.
  Este script encaminha a porta local 3307 para o 3306 do servidor via SSH.

  Enquanto a janela estiver aberta, conecte um cliente (DBeaver, MySQL Workbench,
  mysql CLI) em:
      Host: 127.0.0.1   Porta: 3307
      Usuário/Senha/Database: os mesmos do .env de produção (DB_USER, DB_PASSWORD, DB_NAME)

  Feche a janela (Ctrl+C) para encerrar o túnel.

.PARAMETER Server
  Destino SSH no formato usuario@host (ex.: deploy@meu-servidor.com).

.PARAMETER LocalPort
  Porta local do túnel (padrão 3307, para não conflitar com um MySQL local em 3306).

.EXAMPLE
  ./scripts/db-tunnel.ps1 -Server deploy@meu-servidor.com
#>
param(
    [Parameter(Mandatory = $true)] [string] $Server,
    [int] $LocalPort = 3307
)

Write-Host "Abrindo túnel SSH: localhost:$LocalPort -> (servidor) 127.0.0.1:3306" -ForegroundColor Cyan
Write-Host "Conecte o cliente em 127.0.0.1:$LocalPort. Ctrl+C encerra." -ForegroundColor Yellow
ssh -N -L "${LocalPort}:127.0.0.1:3306" $Server
