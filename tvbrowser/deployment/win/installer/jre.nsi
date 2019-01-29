# NSIS script for creating the JRE Windows installer.
#
#
# TV-Browser
# Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
#
# Author: René Mach


# The following Variables are set from the ANT build script:
#   VERSION_FILE
#   PROG_NAME
#   PROG_NAME_FILE
#   RUNTIME_DIR
#   INSTALLER_DIR
#   PUBLIC_DIR
#   PLATFORM

#--------------------------------
# Includes
#--------------------------------
!AddIncludeDir "${INSTALLER_DIR}"
!include MUI.nsh
!include LogicLib.nsh
!include x64.nsh

#--------------------------------
# Configuration
#--------------------------------

# program name
Name "${PROG_NAME} ${PLATFORM}"

# The file to write
OutFile "${PUBLIC_DIR}\jre\${PROG_NAME_FILE}_${VERSION}_${PLATFORM}.exe"

# Use LZMA compression
SetCompressor /SOLID lzma

# The icons of the installer
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\orange-install.ico"

# use titles with linebreaks
!define MUI_WELCOMEPAGE_TITLE_3LINES
!define MUI_FINISHPAGE_TITLE_3LINES

#--------------------------------
#Interface Settings

# message box when cancelling the installation
!define MUI_ABORTWARNING
# use small description box on bottom of component selection page
!define MUI_COMPONENTSPAGE_SMALLDESC
# always show language selection
!define MUI_LANGDLL_ALWAYSSHOW

#--------------------------------
# Installer pages
#--------------------------------

!insertmacro MUI_PAGE_WELCOME
Page Custom LockedListShow
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

#--------------------------------
# Supported installation languages
#--------------------------------
!insertmacro MUI_LANGUAGE "German"
!insertmacro MUI_LANGUAGE "English"
!include tvbrowser_english.nsh
!include tvbrowser_german.nsh


#--------------------------------
# reserve files for faster extraction
#ReserveFile "${NSISDIR}\UninstallSettings.ini"
!insertmacro MUI_RESERVEFILE_INSTALLOPTIONS
!insertmacro MUI_RESERVEFILE_LANGDLL

#--------------------------------
# Installer Functions
#--------------------------------

Function CheckMultipleInstance
    System::Call 'kernel32::CreateMutexA(i 0, i 0, t "TV-Browser JRE installation") i .r1 ?e'
    Pop $R0
    StrCmp $R0 0 +3
    MessageBox MB_OK|MB_ICONEXCLAMATION "The installer is already running."
    Abort
FunctionEnd

Function .onInit
    call CheckMultipleInstance
    # have language selection for the user
    !insertmacro MUI_LANGDLL_DISPLAY
    ${If} ${RunningX64}
      SetRegView 64
    ${EndIf}
  push $0
  # Get Account Type of the current user
  UserInfo::GetAccountType
  pop $1
  StrCmp $1 "Admin" isadmin isnotadmin
  isnotadmin:
  StrCmp $1 "Power" isadmin isnotpower
  isadmin:
  ReadRegStr $0 HKLM "Software\TV-Browser" "Install directory"
  goto goon
  isnotpower:
  ReadRegStr $0 HKCU "Software\TV-Browser" "Install directory"
  
  goon:
  IfErrors errors
  StrCpy $INSTDIR "$0\java\"
  goto end
  errors:
  # The default installation directory
  ${If} ${RunningX64}
      StrCpy $INSTDIR "$PROGRAMFILES64\TV-Browser\java\"
  ${Else}
      StrCpy $INSTDIR "$PROGRAMFILES\TV-Browser\java\"
  ${EndIf}
  
  end:
  pop $0
FunctionEnd

Function LockedListShow
  !insertmacro MUI_HEADER_TEXT "$(LOCKED_LIST_HEADING)" "$(LOCKED_LIST_CAPTION)"
  LockedList::AddModule /NOUNLOAD "\tvbrowser.exe"
  LockedList::AddModule /NOUNLOAD "\tvbrowser_noDD.exe"
  LockedList::AddCaption /NOUNLOAD "TV-Browser*"
  LockedList::Dialog /heading "$(LOCKED_LIST_HEADING)" /caption "$(LOCKED_LIST_CAPTION)" /searching "$(LOCKED_LIST_SEARCHING)" /noprograms "$(LOCKED_LIST_NOPROGRAMS)" /colheadings "$(LOCKED_LIST_APPLICATION)" "$(LOCKED_LIST_PROCESS)" /ignore "$(LOCKED_LIST_IGNORE)"
FunctionEnd

!macro registerFirewall fileName displayText
    nsisFirewall::AddAuthorizedApplication "${fileName}" "${displayText}"
    Pop $0
    ${If} $0 == 0
        DetailPrint "${displayText} added to Firewall exception list"
    ${Else}
        DetailPrint "An error happened while adding ${displayText} to Firewall exception list (result=$0)"
    ${EndIf}
!macroend

#--------------------------------
# The installation types

#InstType "$(INSTALLATION_TYPE_NORMAL)" #"Normal"

#--------------------------------
#Installer Sections

Section "$(STD_SECTION_NAME)" SEC_STANDARD
  # make the section required
  SectionIn 1 RO

  # Set output path to the installation directory.
  StrCpy $0 $INSTDIR "" -5
  
  StrCmp $0 "\java" +2 +1
  StrCpy $INSTDIR "$INSTDIR\java\"
  
  SetOutPath "$INSTDIR"
  File /r "${RUNTIME_DIR}\java\*.*"
  
  !insertmacro registerFirewall "$INSTDIR\bin\java.exe" "Java"
  !insertmacro registerFirewall "$INSTDIR\bin\javaw.exe" "Java"

SectionEnd # main section

#eof
