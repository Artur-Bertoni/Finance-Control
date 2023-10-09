let financialInstitutionData = [[1, 'Instituição 1'], [2, 'Instituição 2']]
let financialInstitutionList = document.getElementById('financial-institution-input')

for (const element of financialInstitutionData) {
    let option = document.createElement('option')
    option.value = element[0]
    option.innerText = element[1]

    financialInstitutionList.appendChild(option)
}