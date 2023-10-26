<?php

include "../../backend/services/FinancialInstitutionService.php";

session_start();

$service = new FinancialInstitutionService();

if (array_key_exists('postFinancialInstitution', $_POST)) {
    $_SESSION['financialInstitutionId'] = "";
    header("Location: ../FinancialInstitution.html");
    exit;
}

if (array_key_exists('itemButton', $_POST)) {
    $_SESSION['financialInstitutionId'] = $_POST['itemButton'];
    header("Location: ../FinancialInstitution.html");
    exit;
}

if (isset($_POST['homeButton'])) {
    header("Location: ../HomePage.html");
    exit;
}

if (isset($_POST['profileButton'])) {
    header("Location: ../User.html");
    exit;
}
