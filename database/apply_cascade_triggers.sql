USE AIGradingDB;
GO

-- 1. Stored Procedure for cascading deletion of Assignments
CREATE OR ALTER PROCEDURE sp_DeleteAssignmentCascade
    @assignment_id INT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        -- A. Unlink RotationalReviews
        UPDATE RotationalReviews
        SET submission_id = NULL
        WHERE submission_id IN (
            SELECT s.submission_id 
            FROM Submissions s 
            WHERE s.assignment_id = @assignment_id
        );

        -- B. Delete GitMetrics
        DELETE FROM GitMetrics
        WHERE submission_id IN (
            SELECT s.submission_id 
            FROM Submissions s 
            WHERE s.assignment_id = @assignment_id
        );

        -- C. Delete AIAnalysisResults
        DELETE FROM AIAnalysisResults
        WHERE submission_id IN (
            SELECT s.submission_id 
            FROM Submissions s 
            WHERE s.assignment_id = @assignment_id
        );

        -- D. Delete MoulinetteResults
        DELETE FROM MoulinetteResults
        WHERE submission_id IN (
            SELECT s.submission_id 
            FROM Submissions s 
            WHERE s.assignment_id = @assignment_id
        );

        -- E. Delete Appeals
        DELETE FROM Appeals
        WHERE grade_id IN (
            SELECT g.grade_id
            FROM Grades g
            JOIN Submissions s ON g.submission_id = s.submission_id
            WHERE s.assignment_id = @assignment_id
        );

        -- F. Delete Grades
        DELETE FROM Grades
        WHERE submission_id IN (
            SELECT s.submission_id 
            FROM Submissions s 
            WHERE s.assignment_id = @assignment_id
        );

        -- G. Delete Submissions
        DELETE FROM Submissions
        WHERE assignment_id = @assignment_id;

        -- H. Finally delete the Assignment itself
        DELETE FROM Assignments
        WHERE assignment_id = @assignment_id;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

-- 2. Trigger for cascading deletion of Submissions
CREATE OR ALTER TRIGGER trg_DeleteSubmissionCascade
ON Submissions
INSTEAD OF DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- A. Unlink RotationalReviews
    UPDATE RotationalReviews
    SET submission_id = NULL
    WHERE submission_id IN (SELECT submission_id FROM deleted);

    -- B. Delete GitMetrics
    DELETE FROM GitMetrics
    WHERE submission_id IN (SELECT submission_id FROM deleted);

    -- C. Delete AIAnalysisResults
    DELETE FROM AIAnalysisResults
    WHERE submission_id IN (SELECT submission_id FROM deleted);

    -- D. Delete MoulinetteResults
    DELETE FROM MoulinetteResults
    WHERE submission_id IN (SELECT submission_id FROM deleted);

    -- E. Delete Appeals
    DELETE FROM Appeals
    WHERE grade_id IN (
        SELECT g.grade_id 
        FROM Grades g 
        JOIN deleted d ON g.submission_id = d.submission_id
    );

    -- F. Delete Grades
    DELETE FROM Grades
    WHERE submission_id IN (SELECT submission_id FROM deleted);

    -- G. Finally delete the Submissions themselves
    DELETE FROM Submissions
    WHERE submission_id IN (SELECT submission_id FROM deleted);
END;
GO
