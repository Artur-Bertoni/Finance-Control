<?php

include "../../backend/services/TransactionService.php";

session_start();

$service = new TransactionService();

if ($_POST['findAllByUser']) {
    $service->findAllByUser($_SESSION['userId']);
}
