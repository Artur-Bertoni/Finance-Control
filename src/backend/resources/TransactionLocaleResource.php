<?php

include "../../backend/services/TransactionLocaleService.php";

session_start();

$service = new TransactionLocaleService();

if ($_POST['findAllByUser']) {
    $service->findAllByUser($_SESSION['userId']);
}
