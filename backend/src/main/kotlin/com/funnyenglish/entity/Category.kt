package com.funnyenglish.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "categories")
data class Category(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val name: String,

    val description: String? = null,

    @Column(name = "icon_url")
    val iconUrl: String? = null,

    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @OneToMany(mappedBy = "category", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val tests: MutableList<Test> = mutableListOf()
)
