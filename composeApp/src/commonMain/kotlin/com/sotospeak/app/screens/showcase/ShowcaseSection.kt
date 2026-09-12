package com.sotospeak.app.screens.showcase

/**
 * Каталог элементов шоукейса дизайн-системы (по образцу DesignElement из
 * tele2-selfcare-android design-system-showcase). Плоский enum — без рефлексии/кодогенерации.
 */
enum class ShowcaseSection(val title: String) {
    Colors("Цвета"),
    Typography("Типографика"),
    Shapes("Формы"),
    Spacing("Отступы"),
    Icons("Иконки"),
    Buttons("Кнопки"),
    Fields("Поля ввода"),
    AppBar("Аппбар"),
    Navigation("Навигация"),
    RecordingKit("Запись (rec kit)"),
    Loaders("Загрузка и скелетоны"),
    Dialogs("Диалоги"),
    States("Состояния")
}
