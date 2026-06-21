import SockJS from 'sockjs-client/dist/sockjs';
import { Client } from '@stomp/stompjs';

let stompClient: Client | null = null;

export type GitSyncMessage = {
  projectId: number;
  status: 'syncing' | 'success' | 'error';
  message: string;
};

export function connectGitSync(
  projectId: number,
  onMessage: (msg: GitSyncMessage) => void,
): () => void {
  const wsUrl = `${window.location.protocol}//${window.location.host}/ws`;

  const client = new Client({
    webSocketFactory: () => new SockJS(wsUrl),
    reconnectDelay: 5000,
    onConnect: () => {
      client.subscribe(`/topic/git-sync/${projectId}`, (frame) => {
        const msg: GitSyncMessage = JSON.parse(frame.body);
        onMessage(msg);
      });
    },
  });

  client.activate();
  stompClient = client;

  return () => {
    client.deactivate();
    stompClient = null;
  };
}
