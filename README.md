# Persona-IM - Gamified Chatbot with Gemini AI

A beautiful Android chat application inspired by Persona 5, now powered by Google's Gemini AI API.

## Features

### Original UI Restored

- ✨ **Animated Background Particles** - Beautiful spring petals floating in the background
- 🎭 **Character Portraits** - Ann, Ryuji, and Yusuke character portraits at the top
- 💬 **Custom Message Bubbles** - Stylized speech bubbles with animations
- 🖤 **Connecting Lines** - Animated black lines connecting messages
- 🎨 **Persona 5 Aesthetic** - Red theme with custom typography (Optima Nova font)
- ⚡ **Smooth Animations** - Scale, fade, and bounce animations for all UI elements

### Gemini AI Integration

- 🤖 **Smart Responses** - Powered by Google's Gemini API
- 💭 **Loading States** - Beautiful loading indicators during AI processing
- ❌ **Error Handling** - Graceful error display for network issues
- 🔄 **Real-time Chat** - Seamless conversation flow

## How It Works

The app combines the original beautiful UI system with modern AI capabilities:

1. **Message Flow**: When you send a message, it gets added to the original `MessagesState` system
2. **API Integration**: Your message is sent to Gemini AI via Retrofit
3. **UI Updates**: AI responses are fed back into the original `TranscriptState` animation system
4. **Visual Effects**: All the original animations (scaling avatars, animated text, connecting
   lines) work perfectly

## Technical Details

### Architecture

- **MVVM Pattern** - Clean separation with `ChatViewModel`
- **Compose UI** - Modern declarative UI with animations
- **Retrofit** - HTTP client for Gemini API calls
- **State Management** - Reactive state with StateFlow and Compose state

### Key Components

- `MainActivity.kt` - Main UI integration
- `ChatViewModel.kt` - API communication and state management
- `TranscriptState.kt` - Original animation system (enhanced)
- `MessagesState.kt` - Message handling (enhanced for dynamic content)
- `BackgroundParticles.kt` - Animated background effects
- `Avatar.kt`, `Entry.kt`, `Reply.kt` - Original message UI components

## Setup

1. Open the project in Android Studio
2. Make sure you have a valid Gemini API key in `MainActivity.kt`
3. Build and run the app
4. Enjoy the beautiful UI with AI-powered conversations!

## Original Features Preserved

All the beautiful original animations and effects are preserved:

- Avatar scaling and rotation animations
- Message bubble expand animations with custom shapes
- Connecting line drawing animations
- Text fade-in effects
- Question mark punctuation animations
- Character portrait layering with random rotations
- Background particle systems (spring petals)

The app now seamlessly blends the original gamified chat experience with modern AI capabilities!