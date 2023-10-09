<?php

include "../../backend/services/FinancialInstitutionService.php";

session_start();

if (isset($_POST['cancelButton'])) {
    header("Location: ../HomePage.html");
    exit;
}

$service = new FinancialInstitutionService();

$financialInstitutionId = $_POST['financialInstitutionId'];
$userId = $_SESSION['userId'];
$name = $_POST["nameField"];
$address = $_POST["addressField"];
$contact = $_POST["contactField"];

if ($financialInstitutionId != "") {
    $service->update($financialInstitutionId, new FinancialInstitutionRequestDTO(
        $userId,
        $name,
        $address,
        $contact
    ));
} else {
    $service->create(new FinancialInstitutionRequestDTO(
        $userId,
        $name,
        $address,
        $contact
    ));
}

header("Location: ../HomePage.html");
