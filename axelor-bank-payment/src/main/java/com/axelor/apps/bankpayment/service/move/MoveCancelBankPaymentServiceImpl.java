package com.axelor.apps.bankpayment.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.move.MoveReverseService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;

public class MoveCancelBankPaymentServiceImpl {
  protected AppBaseService appBaseService;
  protected ReconcileService reconcileService;
  protected MoveReverseService moveReverseService;

  protected void cancelMoves(BankOrder bankOrder) throws AxelorException {
    for (BankOrderLine bankOrderLine : bankOrder.getBankOrderLineList()) {
      if (bankOrderLine.getSenderMove() != null) {
        this.cancelGeneratedMove(bankOrderLine.getSenderMove());
      }

      if (bankOrderLine.getReceiverMove() != null) {
        this.cancelGeneratedMove(bankOrderLine.getReceiverMove());
      }
    }
  }

  protected void cancelGeneratedMove(Move move) throws AxelorException {
    move.setStatusSelect(MoveRepository.STATUS_CANCELED);

    for (MoveLine moveLine : move.getMoveLineList()) {
      for (Reconcile reconcile : moveLine.getDebitReconcileList()) {
        reconcileService.unreconcile(reconcile);
      }

      for (Reconcile reconcile : moveLine.getCreditReconcileList()) {
        reconcileService.unreconcile(reconcile);
      }
    }

    Move reverseMove = moveReverseService.generateReverse(
        move, false, false, false, appBaseService.getTodayDate(move.getCompany()));
  }
}
