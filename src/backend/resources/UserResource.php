<?php

include "../../backend/services/UserService.php";

session_start();

$service = new UserService();

if (array_key_exists('findById', $_POST) && $_SESSION['userId'] != "") {
    $service->findById($_SESSION['userId']);
}
