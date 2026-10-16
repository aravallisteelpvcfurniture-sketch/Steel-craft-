const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- SECURITY BOUNDS TESTS ---

test("Unauthenticated user: cannot read projects", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("projects").get());
});

test("Authenticated user: cannot read another user's project", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("projects").doc("bob_proj").set({
      userId: BOB_UID,
      customerName: "Sharma Ji",
      category: "STEEL_RAILING",
      title: "Balcony Steel Railing",
      status: "IN_FABRICATION",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("projects").doc("bob_proj").get());
});

test("Authenticated user: can create valid project", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("projects").doc("alice_proj_1").set({
      userId: ALICE_UID,
      customerName: "Verma Construction",
      customerPhone: "+91 9876543210",
      siteAddress: "Plot 42, Civil Lines",
      category: "STEEL_RAILING",
      title: "Main Staircase & Balcony 304 Railing",
      status: "MEASUREMENT",
      totalSqft: 180,
      runningFeet: 60,
      ratePerUnit: 850,
      totalAmount: 51000,
      advancePaid: 15000,
      notes: "Toughened glass 10mm with SS 304 handrail",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: cannot create project with mismatched userId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("projects").doc("spoofed_proj").set({
      userId: BOB_UID, // Spoofing Bob's UID
      customerName: "Fake Customer",
      category: "STEEL_RAILING",
      title: "Spoofed Project",
      status: "MEASUREMENT",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can query own projects with userId filter", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("projects").doc("alice_proj").set({
      userId: ALICE_UID,
      customerName: "Alice Client",
      category: "UPVC_DOORS_WINDOWS",
      title: "UPVC Sliding Window",
      status: "QUOTATION",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("projects").where("userId", "==", ALICE_UID).get()
  );
});

test("Authenticated user: cannot query projects without userId filter", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("projects").get());
});
