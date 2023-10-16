<?php

include "../../backend/services/CategoryService.php";

session_start();

$service = new CategoryService();

if ($_POST['findAllByUser']) {
    $service->findAllByUser($_SESSION['userId']);
}
