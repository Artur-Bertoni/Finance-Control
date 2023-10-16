<?php

include "../../backend/services/AccountService.php";

session_start();

$service = new AccountService();

if ($_POST['findAllByUser']) {
    $service->findAllByUser($_SESSION['userId']);
}
