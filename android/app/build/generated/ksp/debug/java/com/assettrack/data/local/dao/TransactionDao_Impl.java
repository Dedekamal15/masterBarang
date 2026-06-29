package com.assettrack.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.assettrack.data.local.TransactionTypeConverter;
import com.assettrack.data.local.entity.TransactionEntity;
import com.assettrack.domain.model.TransactionType;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TransactionDao_Impl implements TransactionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TransactionEntity> __insertionAdapterOfTransactionEntity;

  private final TransactionTypeConverter __transactionTypeConverter = new TransactionTypeConverter();

  private final EntityInsertionAdapter<TransactionEntity> __insertionAdapterOfTransactionEntity_1;

  private final SharedSQLiteStatement __preparedStmtOfMarkEvidenceUploaded;

  public TransactionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTransactionEntity = new EntityInsertionAdapter<TransactionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `transactions` (`id`,`assetId`,`assetName`,`assetSerialNumber`,`type`,`recipientName`,`destination`,`notes`,`timestampMs`,`latitude`,`longitude`,`gpsAccuracyMeters`,`isSynced`,`evidenceFilePath`,`evidenceType`,`isEvidenceUploaded`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TransactionEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getAssetId());
        statement.bindString(3, entity.getAssetName());
        statement.bindString(4, entity.getAssetSerialNumber());
        final String _tmp = __transactionTypeConverter.fromType(entity.getType());
        statement.bindString(5, _tmp);
        statement.bindString(6, entity.getRecipientName());
        statement.bindString(7, entity.getDestination());
        statement.bindString(8, entity.getNotes());
        statement.bindLong(9, entity.getTimestampMs());
        if (entity.getLatitude() == null) {
          statement.bindNull(10);
        } else {
          statement.bindDouble(10, entity.getLatitude());
        }
        if (entity.getLongitude() == null) {
          statement.bindNull(11);
        } else {
          statement.bindDouble(11, entity.getLongitude());
        }
        if (entity.getGpsAccuracyMeters() == null) {
          statement.bindNull(12);
        } else {
          statement.bindDouble(12, entity.getGpsAccuracyMeters());
        }
        final int _tmp_1 = entity.isSynced() ? 1 : 0;
        statement.bindLong(13, _tmp_1);
        if (entity.getEvidenceFilePath() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getEvidenceFilePath());
        }
        if (entity.getEvidenceType() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getEvidenceType());
        }
        final int _tmp_2 = entity.isEvidenceUploaded() ? 1 : 0;
        statement.bindLong(16, _tmp_2);
      }
    };
    this.__insertionAdapterOfTransactionEntity_1 = new EntityInsertionAdapter<TransactionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `transactions` (`id`,`assetId`,`assetName`,`assetSerialNumber`,`type`,`recipientName`,`destination`,`notes`,`timestampMs`,`latitude`,`longitude`,`gpsAccuracyMeters`,`isSynced`,`evidenceFilePath`,`evidenceType`,`isEvidenceUploaded`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TransactionEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getAssetId());
        statement.bindString(3, entity.getAssetName());
        statement.bindString(4, entity.getAssetSerialNumber());
        final String _tmp = __transactionTypeConverter.fromType(entity.getType());
        statement.bindString(5, _tmp);
        statement.bindString(6, entity.getRecipientName());
        statement.bindString(7, entity.getDestination());
        statement.bindString(8, entity.getNotes());
        statement.bindLong(9, entity.getTimestampMs());
        if (entity.getLatitude() == null) {
          statement.bindNull(10);
        } else {
          statement.bindDouble(10, entity.getLatitude());
        }
        if (entity.getLongitude() == null) {
          statement.bindNull(11);
        } else {
          statement.bindDouble(11, entity.getLongitude());
        }
        if (entity.getGpsAccuracyMeters() == null) {
          statement.bindNull(12);
        } else {
          statement.bindDouble(12, entity.getGpsAccuracyMeters());
        }
        final int _tmp_1 = entity.isSynced() ? 1 : 0;
        statement.bindLong(13, _tmp_1);
        if (entity.getEvidenceFilePath() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getEvidenceFilePath());
        }
        if (entity.getEvidenceType() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getEvidenceType());
        }
        final int _tmp_2 = entity.isEvidenceUploaded() ? 1 : 0;
        statement.bindLong(16, _tmp_2);
      }
    };
    this.__preparedStmtOfMarkEvidenceUploaded = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE transactions SET isEvidenceUploaded = 1 WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final TransactionEntity tx, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTransactionEntity.insert(tx);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertIfNotExists(final TransactionEntity tx,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTransactionEntity_1.insert(tx);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markEvidenceUploaded(final String id,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkEvidenceUploaded.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkEvidenceUploaded.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TransactionEntity>> observeAll() {
    final String _sql = "SELECT * FROM transactions ORDER BY timestampMs DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions"}, new Callable<List<TransactionEntity>>() {
      @Override
      @NonNull
      public List<TransactionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAssetId = CursorUtil.getColumnIndexOrThrow(_cursor, "assetId");
          final int _cursorIndexOfAssetName = CursorUtil.getColumnIndexOrThrow(_cursor, "assetName");
          final int _cursorIndexOfAssetSerialNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "assetSerialNumber");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfRecipientName = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientName");
          final int _cursorIndexOfDestination = CursorUtil.getColumnIndexOrThrow(_cursor, "destination");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampMs");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfGpsAccuracyMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "gpsAccuracyMeters");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfEvidenceFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "evidenceFilePath");
          final int _cursorIndexOfEvidenceType = CursorUtil.getColumnIndexOrThrow(_cursor, "evidenceType");
          final int _cursorIndexOfIsEvidenceUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isEvidenceUploaded");
          final List<TransactionEntity> _result = new ArrayList<TransactionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransactionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpAssetId;
            _tmpAssetId = _cursor.getString(_cursorIndexOfAssetId);
            final String _tmpAssetName;
            _tmpAssetName = _cursor.getString(_cursorIndexOfAssetName);
            final String _tmpAssetSerialNumber;
            _tmpAssetSerialNumber = _cursor.getString(_cursorIndexOfAssetSerialNumber);
            final TransactionType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __transactionTypeConverter.toType(_tmp);
            final String _tmpRecipientName;
            _tmpRecipientName = _cursor.getString(_cursorIndexOfRecipientName);
            final String _tmpDestination;
            _tmpDestination = _cursor.getString(_cursorIndexOfDestination);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final long _tmpTimestampMs;
            _tmpTimestampMs = _cursor.getLong(_cursorIndexOfTimestampMs);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final Float _tmpGpsAccuracyMeters;
            if (_cursor.isNull(_cursorIndexOfGpsAccuracyMeters)) {
              _tmpGpsAccuracyMeters = null;
            } else {
              _tmpGpsAccuracyMeters = _cursor.getFloat(_cursorIndexOfGpsAccuracyMeters);
            }
            final boolean _tmpIsSynced;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_1 != 0;
            final String _tmpEvidenceFilePath;
            if (_cursor.isNull(_cursorIndexOfEvidenceFilePath)) {
              _tmpEvidenceFilePath = null;
            } else {
              _tmpEvidenceFilePath = _cursor.getString(_cursorIndexOfEvidenceFilePath);
            }
            final String _tmpEvidenceType;
            if (_cursor.isNull(_cursorIndexOfEvidenceType)) {
              _tmpEvidenceType = null;
            } else {
              _tmpEvidenceType = _cursor.getString(_cursorIndexOfEvidenceType);
            }
            final boolean _tmpIsEvidenceUploaded;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsEvidenceUploaded);
            _tmpIsEvidenceUploaded = _tmp_2 != 0;
            _item = new TransactionEntity(_tmpId,_tmpAssetId,_tmpAssetName,_tmpAssetSerialNumber,_tmpType,_tmpRecipientName,_tmpDestination,_tmpNotes,_tmpTimestampMs,_tmpLatitude,_tmpLongitude,_tmpGpsAccuracyMeters,_tmpIsSynced,_tmpEvidenceFilePath,_tmpEvidenceType,_tmpIsEvidenceUploaded);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<TransactionEntity>> search(final String query) {
    final String _sql = "\n"
            + "        SELECT * FROM transactions\n"
            + "        WHERE assetName LIKE '%' || ? || '%'\n"
            + "           OR assetSerialNumber LIKE '%' || ? || '%'\n"
            + "           OR recipientName LIKE '%' || ? || '%'\n"
            + "        ORDER BY timestampMs DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    _argIndex = 3;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions"}, new Callable<List<TransactionEntity>>() {
      @Override
      @NonNull
      public List<TransactionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAssetId = CursorUtil.getColumnIndexOrThrow(_cursor, "assetId");
          final int _cursorIndexOfAssetName = CursorUtil.getColumnIndexOrThrow(_cursor, "assetName");
          final int _cursorIndexOfAssetSerialNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "assetSerialNumber");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfRecipientName = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientName");
          final int _cursorIndexOfDestination = CursorUtil.getColumnIndexOrThrow(_cursor, "destination");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampMs");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfGpsAccuracyMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "gpsAccuracyMeters");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfEvidenceFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "evidenceFilePath");
          final int _cursorIndexOfEvidenceType = CursorUtil.getColumnIndexOrThrow(_cursor, "evidenceType");
          final int _cursorIndexOfIsEvidenceUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isEvidenceUploaded");
          final List<TransactionEntity> _result = new ArrayList<TransactionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransactionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpAssetId;
            _tmpAssetId = _cursor.getString(_cursorIndexOfAssetId);
            final String _tmpAssetName;
            _tmpAssetName = _cursor.getString(_cursorIndexOfAssetName);
            final String _tmpAssetSerialNumber;
            _tmpAssetSerialNumber = _cursor.getString(_cursorIndexOfAssetSerialNumber);
            final TransactionType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __transactionTypeConverter.toType(_tmp);
            final String _tmpRecipientName;
            _tmpRecipientName = _cursor.getString(_cursorIndexOfRecipientName);
            final String _tmpDestination;
            _tmpDestination = _cursor.getString(_cursorIndexOfDestination);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final long _tmpTimestampMs;
            _tmpTimestampMs = _cursor.getLong(_cursorIndexOfTimestampMs);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final Float _tmpGpsAccuracyMeters;
            if (_cursor.isNull(_cursorIndexOfGpsAccuracyMeters)) {
              _tmpGpsAccuracyMeters = null;
            } else {
              _tmpGpsAccuracyMeters = _cursor.getFloat(_cursorIndexOfGpsAccuracyMeters);
            }
            final boolean _tmpIsSynced;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_1 != 0;
            final String _tmpEvidenceFilePath;
            if (_cursor.isNull(_cursorIndexOfEvidenceFilePath)) {
              _tmpEvidenceFilePath = null;
            } else {
              _tmpEvidenceFilePath = _cursor.getString(_cursorIndexOfEvidenceFilePath);
            }
            final String _tmpEvidenceType;
            if (_cursor.isNull(_cursorIndexOfEvidenceType)) {
              _tmpEvidenceType = null;
            } else {
              _tmpEvidenceType = _cursor.getString(_cursorIndexOfEvidenceType);
            }
            final boolean _tmpIsEvidenceUploaded;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsEvidenceUploaded);
            _tmpIsEvidenceUploaded = _tmp_2 != 0;
            _item = new TransactionEntity(_tmpId,_tmpAssetId,_tmpAssetName,_tmpAssetSerialNumber,_tmpType,_tmpRecipientName,_tmpDestination,_tmpNotes,_tmpTimestampMs,_tmpLatitude,_tmpLongitude,_tmpGpsAccuracyMeters,_tmpIsSynced,_tmpEvidenceFilePath,_tmpEvidenceType,_tmpIsEvidenceUploaded);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> observePendingCount() {
    final String _sql = "SELECT COUNT(*) FROM transactions WHERE isSynced = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getById(final String id,
      final Continuation<? super TransactionEntity> $completion) {
    final String _sql = "SELECT * FROM transactions WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TransactionEntity>() {
      @Override
      @Nullable
      public TransactionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAssetId = CursorUtil.getColumnIndexOrThrow(_cursor, "assetId");
          final int _cursorIndexOfAssetName = CursorUtil.getColumnIndexOrThrow(_cursor, "assetName");
          final int _cursorIndexOfAssetSerialNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "assetSerialNumber");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfRecipientName = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientName");
          final int _cursorIndexOfDestination = CursorUtil.getColumnIndexOrThrow(_cursor, "destination");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampMs");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfGpsAccuracyMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "gpsAccuracyMeters");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfEvidenceFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "evidenceFilePath");
          final int _cursorIndexOfEvidenceType = CursorUtil.getColumnIndexOrThrow(_cursor, "evidenceType");
          final int _cursorIndexOfIsEvidenceUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isEvidenceUploaded");
          final TransactionEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpAssetId;
            _tmpAssetId = _cursor.getString(_cursorIndexOfAssetId);
            final String _tmpAssetName;
            _tmpAssetName = _cursor.getString(_cursorIndexOfAssetName);
            final String _tmpAssetSerialNumber;
            _tmpAssetSerialNumber = _cursor.getString(_cursorIndexOfAssetSerialNumber);
            final TransactionType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __transactionTypeConverter.toType(_tmp);
            final String _tmpRecipientName;
            _tmpRecipientName = _cursor.getString(_cursorIndexOfRecipientName);
            final String _tmpDestination;
            _tmpDestination = _cursor.getString(_cursorIndexOfDestination);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final long _tmpTimestampMs;
            _tmpTimestampMs = _cursor.getLong(_cursorIndexOfTimestampMs);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final Float _tmpGpsAccuracyMeters;
            if (_cursor.isNull(_cursorIndexOfGpsAccuracyMeters)) {
              _tmpGpsAccuracyMeters = null;
            } else {
              _tmpGpsAccuracyMeters = _cursor.getFloat(_cursorIndexOfGpsAccuracyMeters);
            }
            final boolean _tmpIsSynced;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_1 != 0;
            final String _tmpEvidenceFilePath;
            if (_cursor.isNull(_cursorIndexOfEvidenceFilePath)) {
              _tmpEvidenceFilePath = null;
            } else {
              _tmpEvidenceFilePath = _cursor.getString(_cursorIndexOfEvidenceFilePath);
            }
            final String _tmpEvidenceType;
            if (_cursor.isNull(_cursorIndexOfEvidenceType)) {
              _tmpEvidenceType = null;
            } else {
              _tmpEvidenceType = _cursor.getString(_cursorIndexOfEvidenceType);
            }
            final boolean _tmpIsEvidenceUploaded;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsEvidenceUploaded);
            _tmpIsEvidenceUploaded = _tmp_2 != 0;
            _result = new TransactionEntity(_tmpId,_tmpAssetId,_tmpAssetName,_tmpAssetSerialNumber,_tmpType,_tmpRecipientName,_tmpDestination,_tmpNotes,_tmpTimestampMs,_tmpLatitude,_tmpLongitude,_tmpGpsAccuracyMeters,_tmpIsSynced,_tmpEvidenceFilePath,_tmpEvidenceType,_tmpIsEvidenceUploaded);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getExistingIds(final List<String> ids,
      final Continuation<? super List<String>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT id FROM transactions WHERE id IN (");
    final int _inputSize = ids.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : ids) {
      _statement.bindString(_argIndex, _item);
      _argIndex++;
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item_1;
            _item_1 = _cursor.getString(0);
            _result.add(_item_1);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPending(final Continuation<? super List<TransactionEntity>> $completion) {
    final String _sql = "SELECT * FROM transactions WHERE isSynced = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TransactionEntity>>() {
      @Override
      @NonNull
      public List<TransactionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAssetId = CursorUtil.getColumnIndexOrThrow(_cursor, "assetId");
          final int _cursorIndexOfAssetName = CursorUtil.getColumnIndexOrThrow(_cursor, "assetName");
          final int _cursorIndexOfAssetSerialNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "assetSerialNumber");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfRecipientName = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientName");
          final int _cursorIndexOfDestination = CursorUtil.getColumnIndexOrThrow(_cursor, "destination");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampMs");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfGpsAccuracyMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "gpsAccuracyMeters");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfEvidenceFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "evidenceFilePath");
          final int _cursorIndexOfEvidenceType = CursorUtil.getColumnIndexOrThrow(_cursor, "evidenceType");
          final int _cursorIndexOfIsEvidenceUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isEvidenceUploaded");
          final List<TransactionEntity> _result = new ArrayList<TransactionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransactionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpAssetId;
            _tmpAssetId = _cursor.getString(_cursorIndexOfAssetId);
            final String _tmpAssetName;
            _tmpAssetName = _cursor.getString(_cursorIndexOfAssetName);
            final String _tmpAssetSerialNumber;
            _tmpAssetSerialNumber = _cursor.getString(_cursorIndexOfAssetSerialNumber);
            final TransactionType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __transactionTypeConverter.toType(_tmp);
            final String _tmpRecipientName;
            _tmpRecipientName = _cursor.getString(_cursorIndexOfRecipientName);
            final String _tmpDestination;
            _tmpDestination = _cursor.getString(_cursorIndexOfDestination);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final long _tmpTimestampMs;
            _tmpTimestampMs = _cursor.getLong(_cursorIndexOfTimestampMs);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final Float _tmpGpsAccuracyMeters;
            if (_cursor.isNull(_cursorIndexOfGpsAccuracyMeters)) {
              _tmpGpsAccuracyMeters = null;
            } else {
              _tmpGpsAccuracyMeters = _cursor.getFloat(_cursorIndexOfGpsAccuracyMeters);
            }
            final boolean _tmpIsSynced;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_1 != 0;
            final String _tmpEvidenceFilePath;
            if (_cursor.isNull(_cursorIndexOfEvidenceFilePath)) {
              _tmpEvidenceFilePath = null;
            } else {
              _tmpEvidenceFilePath = _cursor.getString(_cursorIndexOfEvidenceFilePath);
            }
            final String _tmpEvidenceType;
            if (_cursor.isNull(_cursorIndexOfEvidenceType)) {
              _tmpEvidenceType = null;
            } else {
              _tmpEvidenceType = _cursor.getString(_cursorIndexOfEvidenceType);
            }
            final boolean _tmpIsEvidenceUploaded;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsEvidenceUploaded);
            _tmpIsEvidenceUploaded = _tmp_2 != 0;
            _item = new TransactionEntity(_tmpId,_tmpAssetId,_tmpAssetName,_tmpAssetSerialNumber,_tmpType,_tmpRecipientName,_tmpDestination,_tmpNotes,_tmpTimestampMs,_tmpLatitude,_tmpLongitude,_tmpGpsAccuracyMeters,_tmpIsSynced,_tmpEvidenceFilePath,_tmpEvidenceType,_tmpIsEvidenceUploaded);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPendingEvidenceUpload(
      final Continuation<? super List<TransactionEntity>> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM transactions \n"
            + "        WHERE isSynced = 1 \n"
            + "          AND evidenceFilePath IS NOT NULL \n"
            + "          AND isEvidenceUploaded = 0\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TransactionEntity>>() {
      @Override
      @NonNull
      public List<TransactionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAssetId = CursorUtil.getColumnIndexOrThrow(_cursor, "assetId");
          final int _cursorIndexOfAssetName = CursorUtil.getColumnIndexOrThrow(_cursor, "assetName");
          final int _cursorIndexOfAssetSerialNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "assetSerialNumber");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfRecipientName = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientName");
          final int _cursorIndexOfDestination = CursorUtil.getColumnIndexOrThrow(_cursor, "destination");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampMs");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfGpsAccuracyMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "gpsAccuracyMeters");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfEvidenceFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "evidenceFilePath");
          final int _cursorIndexOfEvidenceType = CursorUtil.getColumnIndexOrThrow(_cursor, "evidenceType");
          final int _cursorIndexOfIsEvidenceUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isEvidenceUploaded");
          final List<TransactionEntity> _result = new ArrayList<TransactionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransactionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpAssetId;
            _tmpAssetId = _cursor.getString(_cursorIndexOfAssetId);
            final String _tmpAssetName;
            _tmpAssetName = _cursor.getString(_cursorIndexOfAssetName);
            final String _tmpAssetSerialNumber;
            _tmpAssetSerialNumber = _cursor.getString(_cursorIndexOfAssetSerialNumber);
            final TransactionType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __transactionTypeConverter.toType(_tmp);
            final String _tmpRecipientName;
            _tmpRecipientName = _cursor.getString(_cursorIndexOfRecipientName);
            final String _tmpDestination;
            _tmpDestination = _cursor.getString(_cursorIndexOfDestination);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final long _tmpTimestampMs;
            _tmpTimestampMs = _cursor.getLong(_cursorIndexOfTimestampMs);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final Float _tmpGpsAccuracyMeters;
            if (_cursor.isNull(_cursorIndexOfGpsAccuracyMeters)) {
              _tmpGpsAccuracyMeters = null;
            } else {
              _tmpGpsAccuracyMeters = _cursor.getFloat(_cursorIndexOfGpsAccuracyMeters);
            }
            final boolean _tmpIsSynced;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_1 != 0;
            final String _tmpEvidenceFilePath;
            if (_cursor.isNull(_cursorIndexOfEvidenceFilePath)) {
              _tmpEvidenceFilePath = null;
            } else {
              _tmpEvidenceFilePath = _cursor.getString(_cursorIndexOfEvidenceFilePath);
            }
            final String _tmpEvidenceType;
            if (_cursor.isNull(_cursorIndexOfEvidenceType)) {
              _tmpEvidenceType = null;
            } else {
              _tmpEvidenceType = _cursor.getString(_cursorIndexOfEvidenceType);
            }
            final boolean _tmpIsEvidenceUploaded;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsEvidenceUploaded);
            _tmpIsEvidenceUploaded = _tmp_2 != 0;
            _item = new TransactionEntity(_tmpId,_tmpAssetId,_tmpAssetName,_tmpAssetSerialNumber,_tmpType,_tmpRecipientName,_tmpDestination,_tmpNotes,_tmpTimestampMs,_tmpLatitude,_tmpLongitude,_tmpGpsAccuracyMeters,_tmpIsSynced,_tmpEvidenceFilePath,_tmpEvidenceType,_tmpIsEvidenceUploaded);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object markSynced(final List<String> ids, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE transactions SET isSynced = 1 WHERE id IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : ids) {
          _stmt.bindString(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
