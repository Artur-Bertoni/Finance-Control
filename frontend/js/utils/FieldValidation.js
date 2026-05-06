export function setupRequiredFieldValidation(requiredFields) {
  requiredFields.forEach(fieldId => {
    const element = document.getElementById(fieldId)
    if (!element) return

    element.addEventListener('change', () => removeFieldError(fieldId))
    element.addEventListener('input', () => removeFieldError(fieldId))

    if (element.type === 'radio') {
      document.querySelectorAll(`input[name="${element.name}"]`).forEach(radio => {
        radio.addEventListener('change', () => removeFieldError(fieldId))
      })
    }
  })
}

export function validateRequiredFields(requiredFields, fieldLabels = {}) {
  const emptyFields = []

  requiredFields.forEach(fieldId => {
    const element = document.getElementById(fieldId)
    if (!element) return

    let isEmpty = false

    if (element.type === 'radio') {
      const radioGroup = document.querySelectorAll(`input[name="${element.name}"]`)
      isEmpty = !Array.from(radioGroup).some(radio => radio.checked)
    } else {
      isEmpty = !element.value || element.value.trim() === ''
    }

    if (isEmpty) {
      addFieldError(fieldId)
      const label = fieldLabels[fieldId] || fieldId
      emptyFields.push(label)
    }
  })

  return emptyFields
}

export function addFieldError(fieldId) {
  const element = document.getElementById(fieldId)
  if (!element) return

  if (element.type === 'radio') {
    document.querySelectorAll(`input[name="${element.name}"]`).forEach(radio => {
      radio.parentElement?.classList.add('field-error')
    })
  } else {
    element.classList.add('field-error')
  }
}

export function removeFieldError(fieldId) {
  const element = document.getElementById(fieldId)
  if (!element) return

  if (element.type === 'radio') {
    document.querySelectorAll(`input[name="${element.name}"]`).forEach(radio => {
      radio.parentElement?.classList.remove('field-error')
    })
  } else {
    element.classList.remove('field-error')
  }
}
