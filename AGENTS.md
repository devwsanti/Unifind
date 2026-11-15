# Unifind - Campus Room Locator (Batangas State University TNEU ARASOF - Nasugbu)

## Stack
- Kotlin, Jetpack Compose, Material 3, package com.example.unifind
- Fully offline. Data is loaded from assets/campus.json
- No external libraries unless I approve them (kotlinx.serialization is allowed)

## OOP rules (this is an OOP class project)
- One class per file
- Encapsulation: private fields with controlled access
- Use abstract class Location, subclasses Gate, Facility, Building
- Use interfaces Searchable and Navigable
- Add short KDoc comments naming the OOP concept used in each class

## Style
- Keep code simple and readable, because I must explain it to my professor
- UI strings in English

## Editing rules for the agent
- Edit source files with the IDE editing tools. Do NOT use shell commands
  (Set-Content, python, sed) to rewrite source files.
- Do one small task per prompt. Do not scan the whole project.
- After the change, run only the build and the relevant tests.