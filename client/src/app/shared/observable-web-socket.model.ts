import {EventEmitter} from '@angular/core';
import { Subject, Observable } from 'rxjs';

export class ObservableWebSocket {

    private _messages: EventEmitter<string> = new EventEmitter<string>();
    private reconnectEnabled;
    private reconnectDelay = 5000;
    private ws: WebSocket;

    get messages(): Observable<string> {
        return this._messages;
    }

    constructor(private url: string) { }

    public connect(): Observable<string> {
        if (this.ws) {
            this.disconnect();
        }
        this.ws = this.create();
        this.reconnectEnabled = true;
        return this.messages;
    }

    public disconnect(code?: number, reason?: string): void {
        this.reconnectEnabled = false;
        this.ws.close(code, reason);
        this.ws = null;
    }

    public send(message: string) {
        if (!this.ws) {
            throw new Error("WebSocket is not initialized yet!")
        }
        this.ws.send(message);
    }

    private create(): WebSocket {
        let ws = new WebSocket(this.url);

        ws.onmessage = (msg) => this._messages.next(msg.data);
        ws.onerror = (error) => this._messages.error(error);
        ws.onclose = () => {
            if (this.reconnectEnabled) {
                this.reconnect();
            } else {
                this._messages.complete();
            }
        };

        return ws;
    }

    private reconnect(): void {
        setTimeout(this.create, this.reconnectDelay)
    }

}
