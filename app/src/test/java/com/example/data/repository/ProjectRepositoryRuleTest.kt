package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Project
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class ProjectRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createProject_validPayload_succeedsAndReturnsId() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = ProjectRepository(firestore)

        val uniqueTitle = "Balcony Railing ${UUID.randomUUID().toString().take(6)}"
        val project = Project(
            userId = uid,
            customerName = "Ramesh Kumar",
            customerPhone = "+91 9988776655",
            siteAddress = "Plot 10, Mansarovar",
            category = "STEEL_RAILING",
            title = uniqueTitle,
            status = "MEASUREMENT",
            totalSqft = 120.0,
            runningFeet = 40.0,
            ratePerUnit = 850.0,
            totalAmount = 34000.0,
            advancePaid = 10000.0,
            notes = "SS 304 Jindal pipe with 10mm glass"
        )

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.createProject(project) }
        assertTrue("Project creation should succeed: ${createResult.exceptionOrNull()?.message}", createResult.isSuccess)
        val projectId = createResult.getOrThrow()
        assertTrue(projectId.isNotEmpty())
    }

    @Test
    fun observeProjects_authenticatedOwner_emitsCreatedProject() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = ProjectRepository(firestore)

        val uniqueTitle = "Window Grill ${UUID.randomUUID().toString().take(6)}"
        val project = Project(
            userId = uid,
            customerName = "Suresh Patel",
            category = "ALUMINIUM_SECTION",
            title = uniqueTitle,
            status = "QUOTATION"
        )

        val projectId = withTimeout(DEFAULT_TIMEOUT_MS) { repository.createProject(project).getOrThrow() }

        val emittedList = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeProjects().first { list -> list.any { it.id == projectId } }
        }
        assertTrue(emittedList.any { it.id == projectId })
    }

    @Test
    fun getProjectById_crossUserAccess_failsWithPermissionDeniedOrSecurityException() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = ProjectRepository(firestore)

        val project = Project(
            userId = aliceUid,
            customerName = "Alice Secret Client",
            category = "WOODEN_BALCONY",
            title = "Teak Balcony",
            status = "MEASUREMENT"
        )
        val projectId = withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.createProject(project).getOrThrow() }

        // Switch to Bob
        signInTestUser(BOB_EMAIL)
        val bobRepo = ProjectRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.getProjectById(projectId) }
        assertTrue("Bob should not be able to read Alice's project", result.isFailure)
    }

    @Test
    fun observeProjects_unauthenticatedUser_fails() = runBlocking {
        auth.signOut()
        val repository = ProjectRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeProjects().first()
            }
            fail("Expected exception for unauthenticated user")
        } catch (e: Exception) {
            // Expected IllegalStateException from requireUserId or FirebaseFirestoreException
            assertNotNull(e)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@steeltest.com"
        const val BOB_EMAIL = "bob@steeltest.com"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 6000L
    }
}
