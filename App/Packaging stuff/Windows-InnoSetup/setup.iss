[Setup]
AppName="OpenLDAT"
AppVersion="1.0.0"
AppPublisher="Federico Dossena"
AppPublisherURL="https://openldat.fdossena.com"
AppSupportURL="https://openldat.fdossena.com"
AppUpdatesURL="https://openldat.fdossena.com"
ArchitecturesInstallIn64BitMode=x64
DefaultDirName={pf}\OpenLDAT
DefaultGroupName="OpenLDAT"
DisableProgramGroupPage=yes
LicenseFile=gpl-3.0.txt
OutputDir=.
OutputBaseFilename=OpenLDAT_Setup
Compression=lzma2/ultra64
LZMAAlgorithm=1
LZMAMatchFinder=BT
SolidCompression=yes
LZMANumBlockThreads=1
LZMANumFastBytes=273
LZMADictionarySize=1048576
LZMAUseSeparateProcess=yes
InternalCompressLevel=ultra64
SetupIconFile="icon.ico"
UninstallDisplayIcon="icon.ico"
MinVersion=10.0.17763
PrivilegesRequired=admin

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs sortfilesbyextension;
Source: "openldat\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs sortfilesbyextension;

[Icons]
Name: "{group}\OpenLDAT"; Filename: "{app}\OpenLDAT.exe"
Name: "{commondesktop}\OpenLDAT"; Filename: "{app}\OpenLDAT.exe"

[UninstallDelete]
Type: filesandordirs; Name: "{userappdata}\openldat"
Type: filesandordirs; Name: "{localappdata}\openldat"
