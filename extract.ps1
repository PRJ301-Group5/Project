Add-Type -AssemblyName System.IO.Compression.FileSystem
$docxPath = "c:\Users\Acer\Documents\PRJ Project\AI_Grading_Project_Requirements.docx"
$zip = [System.IO.Compression.ZipFile]::OpenRead($docxPath)
Write-Output "Total entries: $($zip.Entries.Count)"
foreach ($e in $zip.Entries) {
    Write-Output "Entry: $($e.FullName) - Size: $($e.Length)"
}
$entry = $zip.GetEntry("word/document.xml")
if ($entry) {
    $stream = $entry.Open()
    $reader = New-Object System.IO.StreamReader($stream)
    $xmlText = $reader.ReadToEnd()
    $reader.Close()
    $stream.Close()
    
    # Save the xml text or extract text
    [xml]$xml = $xmlText
    $ns = New-Object System.Xml.XmlNamespaceManager($xml.NameTable)
    $ns.AddNamespace("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    $pNodes = $xml.SelectNodes("//w:p", $ns)
    Write-Output "Total paragraphs: $($pNodes.Count)"
    
    $extracted = @()
    foreach ($p in $pNodes) {
        $tNodes = $p.SelectNodes(".//w:t", $ns)
        $t = ($tNodes | ForEach-Object { $_.InnerText }) -join ""
        if (-not [string]::IsNullOrWhiteSpace($t)) {
            $extracted += $t
        }
    }
    $extracted | Out-File -FilePath "c:\Users\Acer\Documents\PRJ Project\extracted_requirements.txt" -Encoding utf8
    Write-Output "Extracted $($extracted.Count) non-empty paragraphs to extracted_requirements.txt"
} else {
    Write-Output "word/document.xml not found!"
}
$zip.Dispose()
