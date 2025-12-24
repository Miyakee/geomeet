## Background
This is a technical exercise to be done as part of the software engineer interview process. The goal is to provide an opportunity for you to demonstrate your competencies and provide some baseline material for us to have richer technical discussions during the interview. Our current tech stack involves Javascript, NextJS, and Postgresql. Feel free to use any language or framework of your choice, but bear in mind the afore-mentioned tech stack. LLM use is allowed for this part of the exercise but not the next - you will be expected to build on this exercise during the interview during a roughly 1 hour hands-on session. As a guideline, spend no more than 4 days on this exercise, although earlier submissions are welcomed should you feel that the goals have been sufficiently met. If more time is needed, simply reach out to us via email with the reason.

## Problem Statement
Groups of friends want to decide on a meeting place that requires the "least traveling time" for all of them. We're going to build a web service that helps them with that.

## Task
Design and build an application that meets the following requirements:

1. A user can initiate a session and invite others to join it.

2. When users join a session on invitation, the application obtains their location.

3. Once all users have joined the session and the application has obtained all their locations, the application can calculate a location that, in some reasonable sense, minimizes the distance from all of them and shows it to all users in the session.

    a. You may ignore the feasibility of meeting up at the calculated location for the sake of this exercise.

    b. You may also assume for now that all users are located in Singapore.

    c. Should you need to make any assumptions or trade-offs, document them clearly in your README.md file.

4. The user who initiated the session, and only this initiating user, is able change the meeting location. The updated location is then shown to all users.

5. The user who initiated the session may end the session at any time.

    a. Once the session is ended, all users are notified that the session has ended. If a meeting location was calculated, it should be displayed prominently to all users. If not, it should be indicated that the session was cancelled.

    b. A user should not be able to join a session that has already ended.

## Expected Engineering Qualities
Through this exercise, we are looking to observe your interpretation of good software quality and engineering practices. These may include, but are definitely not limited to, the quality of the user experience, handling of edge cases, the security, performance, and cost of deploying and running your application. Feel free to extend the application with relevant features as long as it continues to serve the above requirements and showcase the qualities you intend to convey.

## Expected Artifacts
1. Commit your code to a github repository and send its link or share its access to us via email.

2. Your repository should contain a document explaining how your application is expected to be deployed and run, including any necessary preparations required of the environment.

--------------------
# Deploy to: https://ttyuuuuuuuuuuuu.us.ci/login

# Implement Requirements:

## Login: 
```
1. Login successfully with username/email
   given User go to Login page
   when user input valid username/email and password
   Then user can go to dashboard

2. Login fail with invalid username/email
   given User go to Login page
   when user input invalid username/email and password
   Then user see 'Invalid credentials' in login page

3. Register success with not exist username/email
   given User go to Login page and clicked 'CREATE ACCOUNT NOW' button
   when user input unique username/email and password with fixed verification code '2025' - assume this is a verification code send to email
   Then user can go to dashboard and see new register username and email

4. Register fail with exist username/email
   given User go to Login page and clicked 'CREATE ACCOUNT NOW' button
   when user input exist username/email and password with fixed verification code '2025' - assume this is a verification code send to email
   Then user see 'Invalid email: existing email or username'
```

## Session:
### Session create:
```
1. Create session success
   given User in dashboard page
   when user click 'CREATE SESSION' button 
   Then user can go to session page with:
       - participant info
       - Meeting Location (empty)
       - Location Tracking (empty)
       - Map(empty)
       - Invite Friends (has invite url and invite code)
```
### Session Join: (requirement 1)
```
1. Invite User join with generate url
   given User click copy button for Invite Link
   when send to others
   Then others can use this link to join session(after login or create new account)

2. User cannot join session with invalid invite code (for security)
   given link by invalid session id or invalid invite code
   when user open link
   Then user can see  "Invalid Session code" or "Invalid invite code"
```
### Session Page:(requirement 2)
```
1. See user participants
   given invited user Tan join the session  
   when initial user Mei stay in session page without refresh 
   Then Tan and Mei both can see  Tan and Mei joined as participants with joined info

2. Track user location (auto get location)
   given participants in session page 
   when toggle Location Tracking button on
   Then participants location will be display in all participants page with map and icon
   
3. Track user location (auto get location off)
   given participants in session page 
   when toggle Location Tracking button off
   Then participants location will be keep last time position and not-update again in all participants page with map and icon

4. Track user location (manual input location - cause first time https not work. so browser obtained location, then use this one)
   given participants in session page but cannot auto get location
   when user input location name
   Then participants location will be display in all participants page with map and icon for input location and auto untracking current location
```
### Optimal Position(requirement 3)
```
1. Calculate Optimal position
   Given more than 2 participants in page
   When user stay in session page
   Then all participants can see 'CALCULATE OPTIMAL LOCATION' button

2. Calculate Optimal position(Yellow icon)
   Given more than 2 participants in page
   When user click 'CALCULATE OPTIMAL LOCATION' button
   Then it will calculate optimal location (minimizes the distance from all of them) and show as a yellow in map  
```
### Meeting Position(requirement 4)
```
1. Set meeting position(Red icon)
   Given user click 'CALCULATE OPTIMAL LOCATION' button and see optimal position
   When initial user click 'SETTING AS MEETING LOCATION' button
   Then it will update for all participants Meeting position section and show as a red icon in the map

2. Change meeting position(Red icon)
   Given a initial user in session page
   When initial user can click edit button in Meeting position(Just for initial user, other participants cannot see)
   Then user can change meeting position and all participants will get latest meeting position without refresh
```
### End Session (requirement 5)
```
1. End Session with meeting position 
   Given a initial user in session page and had set meeting position
   When initial user can see and click 'END SESSION' button in Header(Just for initial user, other participants cannot see)
   Then all participants will receive the msg for session end. and show notification for Final Meeting position

2. End session without meeting position
   Given a initial user in session page without meeting position set up
   When initial user can see and click 'END SESSION' button in Header(Just for initial user, other participants cannot see)
   Then all participants will receive the msg for session end. and show notification for Session called

3. Join session fail when End Session 
   Given valid link for a session
   When initial user end the session
   Then user cannot join this session and see 'Cannot join a session that has ended'
 ```

# Technical Design

BE:
- **JAVA 17 + Springboot**
- **Clean Architecture**: DDD + Clean Architecture for maintainable and testable code
- **WebSocket**: send msg to topic when data changed.
- **Code Quality**: Automated code quality checks with pre-push hooks (Checkstyle, PMD, SpotBugs)
- **High Test Coverage**: 90%+ code coverage requirement with comprehensive unit and integration tests

FE:
- **React 18 + TypeScript+ Material UI (MUI)**
- **WebSocket**: The application uses WebSocket with STOMP protocol for real-time session updates. This enables instant synchronization of session data, participant locations, and status changes across all connected clients.
- **Geo services switch**: can use config to control use which geo service api. to improve availability

Deploy:
- **hook** check before push code
- **Infra As Code**: terraform and infrastructure as code (IaC) and deployment scripts for the GeoMeet application, organized in a modular and maintainable structure.
- **HTTPS/SSL Support**: Automatic SSL certificate management with Let's Encrypt and Nginx
- **Docker Deployment**: Full containerization with Docker Compose for easy deployment


