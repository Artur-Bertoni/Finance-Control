<?php

global $db;

$db = mysqli_connect("127.0.0.1", "user", "test", "finance-control", "3306");

if (!$db) {
    echo "Erro: Falha ao conectar-se com o banco de dados MySQL." . PHP_EOL;
    echo "Erro: " . mysqli_connect_errno() . PHP_EOL;
    exit;
}
