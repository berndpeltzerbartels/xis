# Release Process (Manual ZIP Upload)

This document describes the manual process for releasing a new version by uploading a bundled ZIP file to Sonatype.

**Note:** This is a custom, manual workaround. The standard and recommended way to publish to Maven Central is by using
the `./gradlew publish` task, which handles artifact structure, metadata, and signing correctly. This manual process
should only be used if the standard `publish` task is not functional.

---

### Step 1: Update Version Number

Before creating the release artifacts, update the version number for the project.

1. Open the `gradle.properties` file in the root of the project.
2. Change the `projectVersion` property to the new version number (e.g., `projectVersion=1.2.3`).

### Step 2: Create the Release ZIP

Run the custom Gradle task to bundle all necessary artifacts into a single ZIP file.

1. Open a terminal in the project's root directory.
2. Execute the following command:
   ```bash
   ./gradlew :distribution:createReleaseZip
   ```
3. After the task completes successfully, a ZIP file named `one.xis-<version>.zip` will be created in your user home
   directory (e.g., `/Users/your-username/` on macOS or `C:\Users\your-username\` on Windows).

### Step 3: Upload to Sonatype Central

Upload the generated ZIP file via the Sonatype Central web interface.

1. Navigate to the Sonatype Central Portal: **https://central.sonatype.com/publishing/deployments**
2. Log in using the project's credentials:

* **Username:** `berndpb`
* **Password:** Stored in the `/xis.kdbx` Keepass file.

3. Follow the on-screen instructions to upload the deployment bundle.

* Select the `one.xis-<version>.zip` file from your home directory that you created in Step 2.
* **Important:** The description you provide on the upload form is publicly readable.

### Step 4: Publish the Deployment

After the upload, Sonatype validates the bundle. Once validation is complete, you must manually publish it.

1. Wait for Sonatype to finish the validation process for your upload.
2. Navigate to the deployment details (usually under `Deployments` -> `Deployment Info`).
3. Click the **"Publish"** button to finalize the release and make it available on Maven Central. This can take some
   time.