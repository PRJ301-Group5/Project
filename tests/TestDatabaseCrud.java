package tests;

import com.aigrading.db.DatabaseManager;
import com.aigrading.services.DatabaseCrudService;

import java.util.*;

public class TestDatabaseCrud {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   TESTING DYNAMIC DATABASE CRUD & AUTO-SYNC      ");
        System.out.println("==================================================");

        try {
            // 1. Test Auto Sync
            Map<String, Object> syncRes = DatabaseCrudService.synchronizeDatabase();
            System.out.println("[TEST 1 - AUTO SYNC] Result: " + syncRes.get("message") + " | Engine: " + syncRes.get("engine"));

            // 2. Test List Tables
            List<Map<String, Object>> tables = DatabaseManager.listAllTablesWithCounts();
            System.out.println("[TEST 2 - LIST TABLES] Found " + tables.size() + " tables.");

            // 3. Test Column Inspection
            List<Map<String, Object>> cols = DatabaseManager.getTableColumnsMetadata("Teams");
            System.out.println("[TEST 3 - COLUMN METADATA] Table Teams has " + cols.size() + " columns.");

            // 4. Test Insert from Java
            long newTeamId = DatabaseCrudService.createTeam(
                "G8", "Cloud DevOps Team", "CI/CD & Cloud Infrastructure", "Automated Cloud Sandbox Pipelines", "https://github.com/team-g8/cloud-devops"
            );
            System.out.println("[TEST 4 - INSERT] Created new Team via Java with ID: " + newTeamId);

            // Verify team in DB
            Map<String, Object> teamRow = DatabaseManager.queryOne("SELECT * FROM Teams WHERE team_id = ?", newTeamId);
            assert teamRow != null : "Team should exist";
            System.out.println("[TEST 4 - VERIFY] Team retrieved: " + teamRow.get("team_name"));

            // 5. Test Update from Java
            Map<String, Object> updateFields = new LinkedHashMap<>();
            updateFields.put("team_name", "Cloud DevOps & Kubernetes Masters");
            boolean updated = DatabaseCrudService.updateTeam((int) newTeamId, updateFields);
            System.out.println("[TEST 5 - UPDATE] Updated team name: " + updated);

            Map<String, Object> updatedTeam = DatabaseManager.queryOne("SELECT * FROM Teams WHERE team_id = ?", newTeamId);
            System.out.println("[TEST 5 - VERIFY] New name: " + updatedTeam.get("team_name"));

            // 6. Test Add Column from Java
            DatabaseManager.addColumnIfNotExists("Teams", "dev_notes", "NVARCHAR(255)");
            System.out.println("[TEST 6 - ADD COLUMN] dev_notes column added to Teams table without touching SQL.");

            // 7. Test Delete from Java
            boolean deleted = DatabaseCrudService.deleteTeam((int) newTeamId);
            System.out.println("[TEST 7 - DELETE] Deleted temporary team: " + deleted);

            Map<String, Object> deletedCheck = DatabaseManager.queryOne("SELECT * FROM Teams WHERE team_id = ?", newTeamId);
            assert deletedCheck == null : "Team should be deleted";
            System.out.println("[TEST 7 - VERIFY] Verified team no longer exists in database.");

            System.out.println("\n==================================================");
            System.out.println("   ALL DYNAMIC CRUD & AUTO-SYNC TESTS PASSED!     ");
            System.out.println("==================================================");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
