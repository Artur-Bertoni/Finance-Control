<?php

include "../../backend/services/FinancialInstitutionService.php";

session_start();

$service = new FinancialInstitutionService();

if ($_POST['findAllByUser']) {
    $service->findAllByUser($_SESSION['userId']);
}
