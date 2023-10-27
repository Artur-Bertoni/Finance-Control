<?php

global $db;

$db = mysqli_connect("204.216.145.129", "iea", "iea20232023", "iea");

if (!$db) {
    echo "Erro: Falha ao conectar-se com o banco de dados MySQL." . PHP_EOL;
    echo "Erro: " . mysqli_connect_errno() . PHP_EOL;
    exit;
}
