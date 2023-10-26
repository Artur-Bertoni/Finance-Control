<?php

include "../../backend/services/FinancialInstitutionService.php";

session_start();

$service = new FinancialInstitutionService();

if (array_key_exists('findAllByUser', $_POST)) {
    $service->findAllByUser($_SESSION['userId']);
}

if (array_key_exists('findById', $_POST) && $_SESSION['financialInstitutionId'] != "") {
    $service->findById($_SESSION['financialInstitutionId']);
}
