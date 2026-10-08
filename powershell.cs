using System;
using System.Diagnostics;

class Program {
    static int Main(string[] args) {
        string cmdLine = Environment.CommandLine;
        string argsOnly = "";
        if (cmdLine.Length > 0 && cmdLine[0] == '"') {
            int closeQuote = cmdLine.IndexOf('"', 1);
            if (closeQuote >= 0 && closeQuote + 1 < cmdLine.Length) {
                argsOnly = cmdLine.Substring(closeQuote + 1).TrimStart();
            }
        } else {
            int firstSpace = cmdLine.IndexOf(' ');
            if (firstSpace >= 0 && firstSpace + 1 < cmdLine.Length) {
                argsOnly = cmdLine.Substring(firstSpace + 1).TrimStart();
            }
        }
        var startInfo = new ProcessStartInfo {
            FileName = @"C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe",
            Arguments = argsOnly,
            UseShellExecute = false
        };
        try {
            using (var process = Process.Start(startInfo)) {
                process.WaitForExit();
                return process.ExitCode;
            }
        } catch (Exception ex) {
            Console.Error.WriteLine(ex.Message);
            return 1;
        }
    }
}
