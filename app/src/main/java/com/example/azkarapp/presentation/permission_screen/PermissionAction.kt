package com.example.azkarapp.presentation.permission_screen

import com.example.azkarapp.domain.model.Country

sealed interface PermissionAction {
    /** حفظ البلد المختار؛ onSaved بيتنادى بعد الحفظ علشان الشاشة تكمل للرئيسية. */
    data class SaveCountry(val country: Country, val onSaved: () -> Unit) : PermissionAction
}
