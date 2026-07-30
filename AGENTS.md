# AGENTS.md

# WarisanGo - AI Coding Instructions

## Project Overview

Project Name: WarisanGo

WarisanGo is a web-based heritage food discovery system developed for Malaysia's Visit Malaysia 2026 (VM2026) campaign. The system helps tourists and local users discover heritage food businesses through AI-assisted video analysis, interactive maps, community reviews, and a reward system.

---

# Technology Stack

Frontend
- HTML5
- CSS3
- JavaScript (ES6)
- Thymeleaf

Backend
- Spring Boot 3.x
- Java 21

Database
- Firebase Cloud Firestore

Version Control
- GitHub

Build Tool
- Maven

External Services
- YouTube Data API v3
- Google Maps API
- Firebase Authentication (Google Sign-In)
- AI APIs (Speech-to-Text and LLM)

---

# Project Architecture

Use Spring Boot MVC architecture.
Never violate MVC responsibilities.
Request Flow
Browser
↓
Controller
↓
Service
↓
Repository
↓
Firebase Cloud Firestore

---

# Project Structure

src/main/java/com/warisango/

config/
controller/
service/
repository/
model/
dto/
exception/
util/

src/main/resources/

view/
static/
application.properties

---

# Package Responsibilities

## controller

Responsibilities

- Receive HTTP requests
- Validate request parameters
- Call Service layer
- Return HTML pages or JSON

Never

- Write business logic
- Access Firebase
- Call external APIs directly

Example

BusinessController
↓
BusinessService

---

## service

Responsibilities
Contains all business logic.

Examples

- Search businesses
- Calculate reward points
- AI workflow
- Validate business rules

Services may call multiple repositories.
Never return HTML.

---

## repository

Responsibilities

Only communicate with Firestore.

Contains

- Create
- Read
- Update
- Delete

Never

- Process business logic
- Call external APIs

---

## model

Represents Firestore documents.

Example

User
Business
Review
Badge
AIRecord
VisitHistory

---

## dto

Contains request and response objects.

Examples

LoginRequest
RegisterRequest
BusinessResponse
ReviewRequest

Never use Entity classes directly as API responses.

---

## config

Contains configuration.

Examples

SecurityConfig
FirebaseConfig
WebConfig
GoogleOAuthConfig
YoutubeConfig

---

## exception

Contains custom exceptions.

Examples

BusinessNotFoundException
AIProcessingException
UserNotFoundException
GlobalExceptionHandler

---

## util

Contains helper classes.

Examples

QRCodeGenerator
DateFormatter
ImageCompressor
DistanceCalculator
PointCalculator

---

# Naming Convention

Packages

lowercase

Example

controller
service
repository

Class

PascalCase

BusinessService
ReviewController

Variables

camelCase

businessName
visitHistory

Methods

camelCase

searchBusinesses()
calculateRewardPoints()

Constants

UPPER_CASE

MAX_REVIEW_LENGTH

---

# Java Standards

Use Java 21.

Follow SOLID principles.
Use constructor injection.

Example

GOOD
private final BusinessService businessService;

BAD

@Autowired
private BusinessService businessService;

Never use field injection.

Always use final whenever possible.
Maximum line length
120 characters.

---

# Spring Boot Standards

Always use

@Controller

@RestController

@Service

@Repository

@Configuration

Use ResponseEntity for REST APIs.

Use Validation annotations.

Example

@NotBlank

@NotNull

@Email

---

# HTML Standards

All HTML goes inside

resources/templates

Use semantic HTML.

Use

header
nav
main
section
footer

No inline CSS.
No inline JavaScript.

---

# CSS Standards

All CSS goes inside

resources/static/css

Naming
kebab-case

Examples

business-card
home-banner
review-item

---

# JavaScript Standards

All JavaScript goes inside

resources/static/js

Use ES6.

Prefer

const

instead of

let

Use async/await.

Avoid jQuery.

---

# Images

Store inside

resources/static/images

Use meaningful names.

Example

logo.png
business-default.jpg
hero-banner.jpg

---

# Firebase Standards

Only Repository classes may communicate with Firestore.

Never access Firebase inside Controller.
Never access Firebase inside HTML.

---

# Security Standards

Never hardcode

API keys
Passwords
Firebase credentials

Use

application.properties
or environment variables.
Use GitHub Secrets in CI/CD.

---

# Logging

Use SLF4J.

Never use

System.out.println()

Use

private static final Logger logger =
LoggerFactory.getLogger(ClassName.class);

---

# Exception Handling

Every Service must throw custom exceptions.
Handle exceptions inside

GlobalExceptionHandler

Do not expose stack traces to users.

---

# AI Module

AI functionality consists of four services.

VideoScraperService

Responsibilities

- Search YouTube
- Retrieve video metadata
- Return video URL

SpeechToTextService

Responsibilities

- Convert video audio into transcript

AIExtractionService

Responsibilities

- Send transcript to AI
- Extract structured information
- Return AIRecord

AIService

Responsibilities

Coordinate

Video Search
↓
Speech To Text
↓
Information Extraction
↓
Business Repository

Never call YouTube directly from Controller.

---

# AI Extraction Result

Expected fields

Business Name

Address

State

City

Category

History

Operating Hours

Phone Number

Latitude

Longitude

Source URL

Transcript

Summary

Status

Pending

Approved

Rejected

---

# Business Rules

Only approved businesses are visible to tourists.

Every AI-generated record starts as

Pending.

Only Admin may approve.

Users cannot modify business information.

---

# Review Rules

One verified review per visit.
Rating
1–5 stars.
Users may upload photos.
Average rating updates automatically.

---

# Reward Rules

Award points after
Verified visit
Approved review
Badge milestones
Leaderboard sorted by total points.

---

# Google Maps

Only approved businesses appear.
Current location is optional.
Navigation opens Google Maps.

---

# Google Sign-In

Authentication uses Google OAuth.
Never implement a custom username/password login unless specifically required.

---

# Code Quality

Avoid duplicate code.
Keep methods short.
Prefer composition over inheritance.
Single Responsibility Principle.
One class should have one responsibility.

---

# Documentation

Every public class requires JavaDoc.
Example
/**
 * Handles business search operations.
 */
Every complex method should include comments explaining the algorithm.

---

# Git Standards

Commit format
feat:
fix:

refactor:

docs:

test:

style:

Example

feat: implement AI extraction service

fix: resolve Firebase authentication issue

refactor: simplify reward calculation

---

# Branch Strategy

main

Production-ready code.

develop

Integration branch.

feature/module-name

New feature.

Example

feature/ai-discovery

feature/review-system

feature/google-login

---

# Pull Requests

Every Pull Request should

Compile successfully

Pass tests

Have descriptive title

Be reviewed before merge

---

# Testing

Service layer must be unit tested.

Controller layer should be integration tested.

Mock external APIs.

Never call real APIs during unit tests.

---

# Performance

Avoid unnecessary database queries.

Cache repeated lookups when appropriate.

Use pagination for large lists.

Load images lazily.

---

# General Rules

Never generate placeholder code unless requested.
Never generate TODO comments unless requested.

Always produce production-quality code.

Follow Spring-Boot MVC architecture strictly.

Keep code modular and maintainable.

When generating new functionality, ensure it integrates with the existing project structure and naming conventions.

If a request conflicts with these instructions, explain the conflict and suggest an MVC-compliant solution.