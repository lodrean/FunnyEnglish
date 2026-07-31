package com.funnyenglish.shared

import com.funnyenglish.shared.model.*
import com.funnyenglish.shared.model.Achievement
import com.funnyenglish.shared.model.AchievementCategory
import com.funnyenglish.shared.model.Rarity
import com.funnyenglish.shared.model.AchievementCondition
import com.funnyenglish.shared.model.ConditionType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * Unit tests for data models and serialization
 */
class ModelTest : FunSpec({

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    context("TestListItem Serialization") {
        
        test("serialize and deserialize TestListItem") {
            val original = TestListItem(
                id = "test-123",
                categoryId = "cat-456",
                title = "Colors Test",
                description = "Learn basic colors",
                thumbnailUrl = "http://example.com/image.png",
                difficulty = Difficulty.EASY,
                pointsReward = 10,
                questionsCount = 5,
                userProgress = TestProgressSummary(
                    completed = true,
                    bestScore = 25,
                    maxScore = 30,
                    stars = 2
                )
            )
            
            val serialized = json.encodeToString(original)
            val deserialized = json.decodeFromString<TestListItem>(serialized)
            
            deserialized.id shouldBe original.id
            deserialized.title shouldBe original.title
            deserialized.userProgress?.stars shouldBe 2
        }
        
        test("handle nullable fields correctly") {
            val item = TestListItem(
                id = "test-456",
                categoryId = "cat-789",
                title = "Numbers Test",
                description = null,
                thumbnailUrl = null,
                difficulty = Difficulty.MEDIUM,
                pointsReward = 15,
                questionsCount = 10,
                userProgress = null
            )
            
            val serialized = json.encodeToString(item)
            val deserialized = json.decodeFromString<TestListItem>(serialized)
            
            deserialized.description shouldBe null
            deserialized.thumbnailUrl shouldBe null
            deserialized.userProgress shouldBe null
        }
    }

    context("SubmitTestResult Serialization") {
        
        test("serialize SubmitTestResult with level up") {
            val result = SubmitTestResult(
                score = 30,
                maxScore = 30,
                percentage = 100,
                stars = 3,
                pointsEarned = 25,
                isNewBestScore = true,
                newAchievements = listOf(
                    Achievement(
                        id = "ach-1",
                        code = "perfect_score",
                        name = "Perfect Score",
                        description = "Get 100% on any test",
                        iconUrl = null,
                        category = AchievementCategory.LEARNING,
                        rarity = Rarity.RARE,
                        isHidden = false,
                        condition = AchievementCondition(
                            type = ConditionType.PERFECT_LESSONS,
                            targetValue = 1
                        ),
                        pointsReward = 50
                    )
                ),
                levelUp = LevelUpInfo(
                    previousLevel = 2,
                    newLevel = 3,
                    newTitle = "Intermediate Learner"
                )
            )
            
            val serialized = json.encodeToString(result)
            val deserialized = json.decodeFromString<SubmitTestResult>(serialized)
            
            deserialized.score shouldBe 30
            deserialized.isNewBestScore shouldBe true
            deserialized.levelUp shouldNotBe null
            deserialized.levelUp?.newLevel shouldBe 3
        }
        
        test("serialize SubmitTestResult without level up") {
            val result = SubmitTestResult(
                score = 15,
                maxScore = 30,
                percentage = 50,
                stars = 1,
                pointsEarned = 10,
                isNewBestScore = false,
                newAchievements = emptyList(),
                levelUp = null
            )
            
            val serialized = json.encodeToString(result)
            val deserialized = json.decodeFromString<SubmitTestResult>(serialized)
            
            deserialized.levelUp shouldBe null
            deserialized.newAchievements shouldBe emptyList()
        }
    }

    context("QuestionType Enum") {
        
        test("all question types are serializable") {
            val types = QuestionType.entries
            
            types.forEach { type ->
                val serialized = json.encodeToString(type)
                val deserialized = json.decodeFromString<QuestionType>(serialized)
                deserialized shouldBe type
            }
        }
    }

    context("Progress Calculation") {
        
        test("TestProgressSummary calculates percentage correctly") {
            val progress = TestProgressSummary(
                completed = true,
                bestScore = 20,
                maxScore = 30,
                stars = 2
            )
            
            progress.percentage shouldBe 66
        }
        
        test("TestProgressSummary handles zero maxScore") {
            val progress = TestProgressSummary(
                completed = false,
                bestScore = 0,
                maxScore = 0,
                stars = 0
            )
            
            progress.percentage shouldBe 0
        }
    }
})
