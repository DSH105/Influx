/*
 * This file is part of Influx.
 *
 * Influx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Influx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Influx.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dsh105.influx.help;

import com.dsh105.commodus.paginator.ObjectPaginator;
import com.dsh105.influx.Controller;
import com.dsh105.influx.InfluxManager;
import com.dsh105.influx.help.entry.ExpandedHelpEntry;
import com.dsh105.influx.response.MessagePurpose;

public class ExpandedHelpProvider<S> extends HelpProvider<ExpandedHelpEntry, S> {

    private ObjectPaginator<ExpandedHelpEntry> paginator;

    public ExpandedHelpProvider(InfluxManager<S> manager, HelpProvision provision) {
        super(manager, provision);
        this.paginator = new ObjectPaginator<>(6);
    }

    @Override
    public ExpandedHelpEntry buildHelpEntry(Controller controller) {
        return new ExpandedHelpEntry(this, controller);
    }

    @Override
    protected boolean add(ExpandedHelpEntry entry) {
        if (super.add(entry)) {
            paginator.add(entry);
            return true;
        }
        return false;
    }

    @Override
    protected boolean remove(String group, ExpandedHelpEntry entry) {
        if (super.remove(group, entry)) {
            paginator.remove(entry);
            return true;
        }
        return false;
    }

    @Override
    public <T extends S> void sendPage(T sender, int page) {
        ObjectPaginator<ExpandedHelpEntry> paginator = new ObjectPaginator<>(this.paginator.getPerPage());
        for (ExpandedHelpEntry entry : this.paginator.getRaw()) {
            if (getManager().authorize(sender, entry.getController(), entry.getPermissions())) {
                paginator.add(entry);
            }
        }
        if (!paginator.exists(page)) {
            getManager().respond(sender, getManager().getMessage(MessagePurpose.PAGE_NOT_FOUND, "<page>", page));
            return;
        }
        for (String entry : paginator.getPage(page)) {
            getManager().respondAnonymously(sender, entry);
        }
    }

    public ObjectPaginator<ExpandedHelpEntry> getPaginator() {
        return paginator;
    }
}