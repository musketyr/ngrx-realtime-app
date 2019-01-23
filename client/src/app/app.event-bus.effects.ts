import {Injectable} from '@angular/core';
import {Action} from '@ngrx/store';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {Observable, Subject} from 'rxjs';
import {filter, tap} from 'rxjs/operators';

import {AppEventBusService} from './app.event-bus.service';
import {UserActions} from './user/user.actions';
import {RemoteAction} from './shared/remote-action.model';
import {ObservableWebSocket} from "./shared/observable-web-socket.model";

@Injectable()
export class AppEventBusEffects {

    private webSocket: ObservableWebSocket;

    constructor(private actions$: Actions,
                private appEventBusService: AppEventBusService) {
        this.webSocket = new ObservableWebSocket('wss://jzla12ar4e.execute-api.eu-west-1.amazonaws.com/test');
        this.webSocket.connect();
        this.webSocket.messages.subscribe({
            next: value => console.log('from websocket', value)
        })
    }

    // Listen to all actions and publish remote actions to account event bus
    @Effect({dispatch: false}) remoteAction$: Observable<Action> = this.actions$.pipe(
        filter(action => action instanceof RemoteAction && action.publishedByUser === undefined),
        tap((action: RemoteAction) => {
            this.appEventBusService.publishAction(action);
            this.webSocket.send(JSON.stringify(action));
        })
    );


    @Effect({dispatch: false}) login$: Observable<Action> = this.actions$.pipe(
        ofType(UserActions.Types.LOGIN),
        tap(() => {
            this.appEventBusService.connect();
        })
    );

}
