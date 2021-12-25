import Foundation
import SwiftUI
import shared
import KMPNativeCoroutinesAsync


extension SessionDetails: Identifiable { }
extension SpeakerDetails: Identifiable { }
extension RoomDetails: Identifiable { }

@MainActor
class KikiConfViewModel: ObservableObject {
    let repository = KikiConfRepository()
    @Published public var sessions: [SessionDetails] = []
    @Published public var speakers: [SpeakerDetails] = []
    @Published public var rooms: [RoomDetails] = []
    
    @Published public var enabledLanguages: Set<String> = []
    
    private var sessionsTask: Task<(), Never>? = nil
    private var speakeresTask: Task<(), Never>? = nil
    private var roomsTask: Task<(), Never>? = nil
    
    init() {
        Task {
            do {
                let stream = asyncStream(for: repository.enabledLanguagesNative)
                for try await data in stream {
                    self.enabledLanguages = data
                }
            } catch {
                print("Failed with error: \(error)")
            }
        }
    }

    
    func startObservingSessions() {
        sessionsTask = Task {
            do {
                let stream = asyncStream(for: repository.sessionsNative)
                for try await data in stream {
                    self.sessions = data
                }
            } catch {
                print("Failed with error: \(error)")
            }
        }
    }
    
    func stopObservingSessions() {
        sessionsTask?.cancel()
    }
    
    func toggleLanguageChecked(language: String) {        
        let checked = enabledLanguages.contains(language) ? false : true
        repository.onLanguageChecked(language: language, checked: checked)
    }

    
    func startObservingSpeakers() {
        speakeresTask = Task {
            do {
                let stream = asyncStream(for: repository.speakersNative)
                for try await data in stream {
                    self.speakers = data
                }
            } catch {
                print("Failed with error: \(error)")
            }
        }
    }
    
    func stopObservingSpeakers() {
        speakeresTask?.cancel()
    }

    
    func startObservingRooms() {
        roomsTask = Task {
            do {
                let stream = asyncStream(for: repository.roomsNative)
                for try await data in stream {
                    self.rooms = data
                }
            } catch {
                print("Failed with error: \(error)")
            }
        }
    }
    
    func stopObservingRooms() {
        roomsTask?.cancel()
    }

}

