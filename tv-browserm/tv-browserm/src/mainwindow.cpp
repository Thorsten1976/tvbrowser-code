#include "mainwindow.h"
#include "ui_mainwindow.h"
#include "mymodel.h"
#include <QMessageBox>
#include <QAbstractTableModel>
#include <QStringList>
#include <QMovie>
#include <QDialog>
#include <QSettings>
#include <QDir>
#include <QTextCodec>
#include <QWaitCondition>
#include <QThread>
#include "sqlite_source/sqlite3.h"


MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);

    this->setWindowTitle("TV-BrowserM");


    model = new MyModel(this);

    ui->tableView->setModel(model);
    ui->tableView->setColumnHidden (2, true);
    ui->tableView->setColumnHidden (3, true);
    ui->tableView->setColumnWidth(0,250);
    ui->tableView->setColumnWidth(1,500);
    ui->tableView->setColumnWidth(2,0);

    ui->tableView->setEditTriggers(QAbstractItemView::NoEditTriggers);

#ifdef Q_WS_HILDON
       this->setProperty("FingerScrollable", true);
       ui->tableView->setProperty("FingerScrollable", true);
#endif


    ui->cbTime->addItem(tr("Now"),QVariant("0"));
    ui->cbTime->addItem(tr("in 15 minutes"),QVariant("1"));
    ui->cbTime->addItem(tr("in 30 minutes"),QVariant("2"));
    ui->cbTime->addItem(tr("on 06:00"),QVariant("3"));
    ui->cbTime->addItem(tr("on 12:00"),QVariant("4"));
    ui->cbTime->addItem(tr("on 18:00"),QVariant("5"));
    ui->cbTime->addItem(tr("on 20:15"),QVariant("6"));
    ui->cbTime->addItem(tr("on 22:00"),QVariant("7"));

    ui->cbTime->setCurrentIndex(0);
    ui->dteTime->setDateTime(QDateTime::currentDateTime());


    ui->rbAm->setText(tr("DateTime"));
    ui->rbCB->setText(tr("On TV"));

    ui->rbCB->setChecked(true);



}

MainWindow::~MainWindow()
{
    delete ui;
}

void MainWindow::ClearTable()
{
  int iRows = model->rowCount( QModelIndex());
  for(int i=iRows -1; i >= 0; i = i - 1)
  {
      model->removeRow(i,QModelIndex());
  }

}


void MainWindow::AddToTable(QString sender, QString Sendung, QString VonBis, QString sid)
{
   int iRows = model->rowCount( QModelIndex());

   model->insertRows(iRows, 1, QModelIndex());
   QModelIndex index = model->index(iRows, 0, QModelIndex());
   model->setData(index, sender, Qt::EditRole);
   index = model->index(iRows, 1, QModelIndex());
   model->setData(index, Sendung, Qt::EditRole);
   index = model->index(iRows, 2, QModelIndex());
   model->setData(index, VonBis, Qt::EditRole);
   index = model->index(iRows, 3, QModelIndex());
   model->setData(index, sid, Qt::EditRole);


}

void MainWindow::changeEvent(QEvent *e)
{
    QMainWindow::changeEvent(e);
    switch (e->type()) {
    case QEvent::LanguageChange:
        ui->retranslateUi(this);
        break;
    default:
        break;
    }
}




QString MainWindow::DecryptText(char* sText)
{
    QString sResult = "";
    QString sText2 = "";
    if (sText == NULL)
    {
        return "";
    }
    sText2 = QString::fromUtf8(sText,strlen(sText));
    sText2 = sText2.replace("@_@", "'",Qt::CaseSensitive);

    for (int i = 0; i < sText2.length(); i++)
    {
        QByteArray ba = sText2.toAscii();
        char  cchar = ba.at(i);
        sResult = sResult + QString((char) (int(cchar) - 7));
    }
    return sResult;
}

void MainWindow::LoadTVData(QDateTime dts)
{

#ifdef Q_WS_HILDON
    QString sAppDir = QDir::homePath() + QLatin1String("/MyDocs/tv-browserm");
    QDir dir;
    dir.mkpath(sAppDir);
#else
    QString sAppDir = QApplication::applicationDirPath();
#endif



    QString sID;
    QString lastErrorMessage;
    QString DBd = sAppDir + "/tvexp.tvd";

    QFile f( DBd);
    if( !f.exists() )
    {
      QMessageBox::critical(NULL, tr("No Databasefile"), tr("There is no Databasefile in: \n") + DBd);
      QApplication::quit();
      return;
    }

    sqlite3 *db;
    int err=0;
    err = sqlite3_open(DBd.toUtf8().data(), &db);
    if ( err ) {
        lastErrorMessage = sqlite3_errmsg(db);
        sqlite3_close(db);
        return;
    }
    sqlite3_stmt *vm;
    const char *tail;
    QString statement ="SELECT channel.name,broadcast.id,title,start,end FROM broadcast INNER JOIN channel on channel.id = broadcast.channel_id where  datetime('" + dts.toString("yyyy-MM-dd hh:mm:ss") +"') between start and end order by channel.name";
    sqlite3_prepare(db,statement.toUtf8().data(),statement.toUtf8().length(),&vm, &tail);
    if (err == SQLITE_OK){
        while ( sqlite3_step(vm) == SQLITE_ROW ){
             //QTime dieTime = QTime::currentTime().addSecs(2);
             //while( QTime::currentTime() < dieTime )
             //QCoreApplication::processEvents(QEventLoop::AllEvents, 100);



             char* cChanName    = (char *) sqlite3_column_text(vm, 0);
             char* cBroadcastID = (char *) sqlite3_column_text(vm, 1);
             char* cTitel       = (char *) sqlite3_column_text(vm, 2);
             char* cStart       = (char *) sqlite3_column_text(vm, 3);
             char* cEnd         = (char *) sqlite3_column_text(vm, 4);


             QVariant vStart(cStart);
             QVariant vEnd(cEnd);

             QString sVonBisF = vStart.toDateTime().toString("hh:mm") + " - " + vEnd.toDateTime().toString("hh:mm");
             QString sTitleF  = sVonBisF + " - ";
             QString sChanName = QString::fromUtf8(cChanName,strlen(cChanName));



             sTitleF = sTitleF + " " +  QString::fromUtf8(cTitel,strlen(cTitel));
             AddToTable(sChanName,sTitleF, sVonBisF,QString::fromUtf8(cBroadcastID,strlen(cBroadcastID)));



        }

    }
    sqlite3_finalize(vm);
    sqlite3_close(db);
}


QString MainWindow::GetKurzinfo(QString sSID)
{
#ifdef Q_WS_HILDON
    QString sAppDir = QDir::homePath() + QLatin1String("/MyDocs/tv-browserm");
    QDir dir;
    dir.mkpath(sAppDir);
#else
    QString sAppDir = QApplication::applicationDirPath();
#endif
    QString sID;
    QString lastErrorMessage;
    QString DBd = sAppDir + "/tvexp.tvd";

    QFile f( DBd);
    if( !f.exists() )
    {
      QMessageBox::critical(NULL, tr("No Databasefile"), tr("There is no Databasefile in: \n") + DBd);
      QApplication::quit();
      return "";
    }

    sqlite3 *db;
    int err=0;
    err = sqlite3_open(DBd.toUtf8().data(), &db);
    if ( err ) {
        lastErrorMessage = sqlite3_errmsg(db);
        sqlite3_close(db);
        return "";
    }
    sqlite3_stmt *vm;
    QString sInfo ="";
    const char *tail;
    QString statement ="select shortdescription from info where broadcast_id='" + sSID + "'";
    sqlite3_prepare(db,statement.toUtf8().data(),statement.toUtf8().length(),&vm, &tail);
    if (err == SQLITE_OK){
        while ( sqlite3_step(vm) == SQLITE_ROW ){
            char* Text    = (char *) sqlite3_column_text(vm, 0);

             sInfo = DecryptText(Text);

        }

    }
    sqlite3_finalize(vm);
    sqlite3_close(db);
    return sInfo;
}


void MainWindow::on_pbRefresh_clicked()
{
    QDateTime dts;
    QVariant v;
    ClearTable();

    if (ui->rbCB->isChecked() == true)
    {
        switch(ui->cbTime->currentIndex())
        {
            case 0:
                    dts = QDateTime::currentDateTime();
                    LoadTVData(dts);
                    break;
            case 1:
                    dts = QDateTime::currentDateTime().addSecs(15 * 60);
                    LoadTVData(dts);
                    break;
            case 2:
                    dts = QDateTime::currentDateTime().addSecs(30 * 60);
                    LoadTVData(dts);
                    break;
            case 3:
                    v = QDateTime::currentDateTime().toString("yyyy-MM-dd") + " 06:00:00";
                    dts = v.toDateTime();
                    LoadTVData(dts);
                    break;
            case 4:
                    v = QDateTime::currentDateTime().toString("yyyy-MM-dd") + " 12:00:00";
                    dts = v.toDateTime();
                    LoadTVData(dts);
                    break;
            case 5:
                    v = QDateTime::currentDateTime().toString("yyyy-MM-dd") + " 18:00:00";
                    dts = v.toDateTime();
                    LoadTVData(dts);
                    break;
            case 6:
                    v = QDateTime::currentDateTime().toString("yyyy-MM-dd") + " 20:15:00";
                    dts = v.toDateTime();
                    LoadTVData(dts);
                    break;
            case 7:
                    v = QDateTime::currentDateTime().toString("yyyy-MM-dd") + " 22:00:00";
                    dts = v.toDateTime();
                    LoadTVData(dts);
                    break;
        }

    } else
    {
        dts = ui->dteTime->dateTime();
        LoadTVData(dts);
    }

}

void MainWindow::on_tableView_doubleClicked(QModelIndex index)
{
  QString sTitel = model->getData(index.row(),1);
  QString sSID = model->getData(index.row(),3);
  QString sInfo = GetKurzinfo(sSID);
  QMessageBox::information(NULL, sTitel, sInfo);
}

void MainWindow::on_rbCB_toggled(bool checked)
{
    if (checked == true)
    {
       ui->dteTime->setEnabled(false);
       ui->cbTime->setEnabled(true);
    }
}

void MainWindow::on_rbAm_toggled(bool checked)
{
    if (checked == true)
    {
       ui->dteTime->setEnabled(true);
       ui->cbTime->setEnabled(false);
    }
}
