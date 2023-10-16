import {FinancialInstitution} from "./class/FinancialInstitutionClass.js";

let data;
let financialInstitutions = [];

$.ajax({
    url: 'http://localhost/finance-control/src/backend/resources/FinancialInstitutionResource.php',
    type: 'POST',
    async: false,
    data: {findAllByUser: true},
    success: function (response) {
        data = JSON.parse(response);
        financialInstitutions = processData(data);
        console.log(financialInstitutions)
    },
    error: function (error) {
        console.error(error);
    }
});

function processData(data) {
    let array = [];
    for (const element of data) {
        const financialInstitutionData = element;
        const financialInstitution = new FinancialInstitution(
            Number(financialInstitutionData.id),
            financialInstitutionData.name,
            financialInstitutionData.address,
            financialInstitutionData.contact
        );

        array.push(financialInstitution);
    }

    return array;
}

let financialInstitutionList = document.getElementById('financial-institution-input')

for (const element of financialInstitutions) {
    let option = document.createElement('option')
    option.value = element.id
    option.innerText = element.name

    financialInstitutionList.appendChild(option)
}